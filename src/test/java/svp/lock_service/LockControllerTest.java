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
public class LockControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private static final String localhost = "http://localhost:8080";
    private static final String existsLockByFileEndpoint = "/locker/exists";

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


    @Test
    public void lockExists_NotExistedFile_ExpectError() throws Exception {
        File file = new File("noexisted.adaf");
        MvcResult mvcResult = mockMvc.perform(get(localhost + existsLockByFileEndpoint + "?itemId=" + file.getAbsolutePath()))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        BaseResponse responseModel = BaseResponse.fromJSON(content);
        Assert.assertEquals(Status.ERROR, responseModel.getStatus());
    }

    @Test
    public void lockGrabTest_OneGrabsThenAnotherGrabs_ExpectGrabOnFirstAndNotGrabOnSecond() throws Exception {
        File file = new File("/");
        MockHttpServletRequestBuilder requestBuilder = get(localhost + existsLockByFileEndpoint + "?itemId=" + file.getAbsolutePath());
        MvcResult resultFirstClient = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        BaseResponse responseModelForFirstClient = BaseResponse.fromJSON(resultFirstClient.getResponse().getContentAsString());
        Assert.assertEquals(Status.SUCCESS, responseModelForFirstClient.getStatus());

        MvcResult resultSecondClient = mockMvc.perform(requestBuilder).andReturn();
        BaseResponse responseModelForSecondClient = BaseResponse.fromJSON(resultSecondClient.getResponse().getContentAsString());
        Assert.assertEquals(Status.ERROR, responseModelForSecondClient.getStatus());
    }
}