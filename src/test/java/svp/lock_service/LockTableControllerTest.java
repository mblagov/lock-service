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
import svp.lock_service.controllers.LockTableControllerImpl;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.models.Status;

import javax.servlet.ServletContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LockServiceApplication.class})
@WebAppConfiguration(value = "")
@SpringBootTest
public class LockTableControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private static final String localhost = "http://localhost:8080/tablelocker";
    private static final String existsLockByTableEndpoint = "/checkfree";
    private static final String grabLockByTableEndpoint = "/grab";
    private static final String givebackLockByTableEndpoint = "/giveback";

    private String tableTestPath = "svpbigdata4.messages";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesLockControllerImpl() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(webApplicationContext.getBean(LockTableControllerImpl.class));
    }

    // Zookeeper-5
    @Test
    public void accessTableTest_NoOneHoldLock_OneAccessesTable_ExpectSuccessfulAccess() throws Exception {
        lockHelperRequest(tableTestPath, givebackLockByTableEndpoint);

        BaseResponse responseModelForClient = lockHelperRequest(tableTestPath, grabLockByTableEndpoint);
        Assert.assertEquals(Status.SUCCESS, responseModelForClient.getStatus());

        cleanUpGrabbedLock(tableTestPath);
    }

    // Zookeeper-6
    @Test
    public void lockGrabTest_OneHoldLock_AnotherGrabsSame_ExpectNotGrabOnSecond() throws Exception {
        BaseResponse responseModelForFirstClient = lockHelperRequest(tableTestPath, grabLockByTableEndpoint);
        Assert.assertEquals(Status.SUCCESS, responseModelForFirstClient.getStatus());

        BaseResponse responseModelForSecondClient = lockHelperRequest(tableTestPath, grabLockByTableEndpoint);
        Assert.assertEquals(Status.ERROR, responseModelForSecondClient.getStatus());

        cleanUpGrabbedLock(tableTestPath);
    }

    // Zookeeper-7
    @Test
    public void releaseLockTest_OneHoldLock_OneReleaseGrabbedLock_ExpectLockRelease() throws Exception {
        BaseResponse responseModel = lockHelperRequest(tableTestPath, grabLockByTableEndpoint);
        Assert.assertEquals(Status.SUCCESS, responseModel.getStatus());

        cleanUpGrabbedLock(tableTestPath);
    }

    // Zookeeper-11
    @Test
    public void releaseNotHoldedLockTest_NoOneHoldLock_OneReleaseUnholdLock_ExpectNoLockReleased() throws Exception {
        BaseResponse checkLockFreeModelResponse = lockHelperRequest(tableTestPath, existsLockByTableEndpoint);

        if (checkLockFreeModelResponse.getStatus().equals(Status.ERROR)) {
            BaseResponse givebackResponseModel = lockHelperRequest(tableTestPath, givebackLockByTableEndpoint);
            Assert.assertEquals(Status.SUCCESS,givebackResponseModel.getStatus());
        }
        cleanUpGrabbedLock(tableTestPath);
    }


    private BaseResponse lockHelperRequest(String path, String endpoint) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(localhost + endpoint + "?itemId=" + path);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        return BaseResponse.fromJSON(mvcResult.getResponse().getContentAsString());
    }

    private void cleanUpGrabbedLock(String path) {
        try {
            lockHelperRequest(path, givebackLockByTableEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


