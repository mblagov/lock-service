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
import svp.lock_service.controllers.LockHDFSFileControllerImpl;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.models.Status;

import javax.servlet.ServletContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LockServiceApplication.class})
@WebAppConfiguration(value = "")
@SpringBootTest
public class LockControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private static final String localhost = "http://localhost:8080";
    private static final String existsLockByFileEndpoint = "/locker/exists";
    private static final String grabLockByFileEndpoint = "/locker/grab";
    private static final String givebackLockByFileEndpoint = "/locker/giveback";

    private String hdfsTestPath = "hdfs://n56:8020/user/students/kafka-reader/text.txt";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesLockControllerImpl() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(webApplicationContext.getBean(LockHDFSFileControllerImpl.class));
    }

    // Zookeeper-1
    @Test
    public void accessFileTest_NoOneHoldLock_OneAccessesFile_ExpectSuccessfulAccess() throws Exception {
        lockHelperRequest(hdfsTestPath, givebackLockByFileEndpoint);

        BaseResponse responseModelForClient = lockHelperRequest(hdfsTestPath, grabLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, responseModelForClient.getStatus());

        cleanUpGrabbedLock(hdfsTestPath);
    }

    // Zookeeper-2
    @Test
    public void lockGrabTest_OneHoldLock_AnotherGrabsSame_ExpectNotGrabOnSecond() throws Exception {
        BaseResponse responseModelForFirstClient = lockHelperRequest(hdfsTestPath, grabLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, responseModelForFirstClient.getStatus());

        BaseResponse responseModelForSecondClient = lockHelperRequest(hdfsTestPath, grabLockByFileEndpoint);
        Assert.assertEquals(Status.ERROR, responseModelForSecondClient.getStatus());

        cleanUpGrabbedLock(hdfsTestPath);
    }

    // Zookeeper-3
    @Test
    public void releaseLockTest_OneHoldLock_OneReleaseGrabbedLock_ExpectLockRelease() throws Exception {
        BaseResponse grabResponseModel = lockHelperRequest(hdfsTestPath, grabLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, grabResponseModel.getStatus());

        BaseResponse givebackResponseModel = lockHelperRequest(hdfsTestPath, givebackLockByFileEndpoint);
        Assert.assertEquals(Status.SUCCESS, givebackResponseModel.getStatus());
    }

    // Zookeeper-4
    @Test
    public void releaseNotHoldedLockTest_NoOneHoldLock_OneReleaseUnholdLock_ExpectNoLockReleased() throws Exception {
        BaseResponse existsResponseModel = lockHelperRequest(hdfsTestPath, existsLockByFileEndpoint);

        if (existsResponseModel.getStatus().equals(Status.SUCCESS)) {
            BaseResponse givebackResponseModel = lockHelperRequest(hdfsTestPath, givebackLockByFileEndpoint);
            Assert.assertEquals(Status.SUCCESS, givebackResponseModel.getStatus());
        }

        BaseResponse givebackResponseModel = lockHelperRequest(hdfsTestPath, givebackLockByFileEndpoint);
        Assert.assertEquals(Status.ERROR, givebackResponseModel.getStatus());
    }

    // Zookeeper-8
    @Test
    public void lockExists_NotExistedFile_ExpectError() throws Exception {
        String notExistingPath = "fjgoagpagagaha";
        BaseResponse existsResponse = lockHelperRequest(notExistingPath, existsLockByFileEndpoint);
        Assert.assertEquals(Status.ERROR, existsResponse.getStatus());
    }

    private BaseResponse lockHelperRequest(String path, String endpoint) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(localhost + endpoint + "?itemId=" + path);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
        return BaseResponse.fromJSON(mvcResult.getResponse().getContentAsString());
    }

    private void cleanUpGrabbedLock(String path) {
        try {
            lockHelperRequest(path, givebackLockByFileEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}