package svp.lock_service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import svp.lock_service.controllers.LockControllerImpl;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.models.Status;

import javax.servlet.ServletContext;
import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LockServiceApplication.class})
@WebAppConfiguration(value = "")
@SpringBootTest
public class LockControllerTest extends Assert {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private static final String localhost = "http://localhost:8080";
    private static final String existsLockByFileEndpoint = "/locker/exists";
    private static final String grabLockByFileEndpoint = "/locker/grab";
    private static final String givebackLockByFileEndpoint = "/locker/giveback";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesLockControllerImpl() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(webApplicationContext.getBean(LockControllerImpl.class));
    }


    // Zookeper-2
    @Test
    public void lockGrabTest_OneHoldLock_AnotherGrabsSame_ExpectNotGrabOnSecond() throws Exception {
        File file = new File("/");
        MockHttpServletRequestBuilder requestBuilder = get(localhost + existsLockByFileEndpoint + "?itemId=" + file.getAbsolutePath());
        MvcResult resultFirstClient = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        BaseResponse responseModelForFirstClient = BaseResponse.fromJSON(resultFirstClient.getResponse().getContentAsString());
        Assert.assertEquals(Status.SUCCESS, responseModelForFirstClient.getStatus());

        MvcResult resultSecondClient = mockMvc.perform(requestBuilder).andReturn();
        BaseResponse responseModelForSecondClient = BaseResponse.fromJSON(resultSecondClient.getResponse().getContentAsString());
        Assert.assertEquals(Status.ERROR, responseModelForSecondClient.getStatus());
    }

    // Zookeper-3
    @Test
    public void releaseLockTest_OneHoldLock_OneReleaseGrabbedLock_ExpectLockRelease() throws Exception {
        File file = new File("/");
        BaseResponse grabResponseModel = lockHelperRequest(file, grabLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, grabResponseModel.getStatus());

        BaseResponse givebackResponseModel = lockHelperRequest(file, givebackLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, givebackResponseModel.getStatus());
    }

    // Zookeper-4
    @Test
    public void releaseNotHoldedLockTest_NoOneHoldLock_OneReleaseUnholdLock_ExpectNoLockReleased() throws Exception {
        File file = new File("/");

        BaseResponse existsResponseModel = lockHelperRequest(file, existsLockByFileEndpoint);

        if (existsResponseModel.getStatus().equals(Status.SUCCESS)) {
            BaseResponse givebackResponseModel = lockHelperRequest(file, givebackLockByFileEndpoint);
            Assert.assertEquals(Status.SUCCESS, givebackResponseModel.getStatus());
        }

        BaseResponse givebackResponseModel = lockHelperRequest(file, givebackLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, givebackResponseModel.getStatus());
    }

    // Zookeper-8
    @Test
    public void lockExists_NotExistedFile_ExpectError() throws Exception {
        File file = new File("noexisted.adaf");
        BaseResponse existsResponse = lockHelperRequest(file, existsLockByFileEndpoint);
        Assert.assertEquals(Status.ERROR, existsResponse.getStatus());
    }

    private BaseResponse lockHelperRequest(File file, String endpoint) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(localhost + endpoint + "?itemId=" + file.getAbsolutePath());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        return BaseResponse.fromJSON(mvcResult.getResponse().getContentAsString());
    }
}