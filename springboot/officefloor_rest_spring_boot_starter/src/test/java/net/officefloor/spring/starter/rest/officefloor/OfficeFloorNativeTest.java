package net.officefloor.spring.starter.rest.officefloor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.OfficeFloorSpecificTest;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms native {@link net.officefloor.frame.api.manage.OfficeFloor} functionality can sit beside Spring.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorNativeTest extends AbstractMockMvcVerification {

    @Test
    public void pathParameter() throws Exception {
        this.mvc.perform(get(this.getPath("/path/1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ID=1")));
    }

    public static class ServicePathParameter {
        public void service(@HttpPathParameter("id") String id, ObjectResponse<OfficeFloorSpecificTest.Response> response) {
            response.send(new OfficeFloorSpecificTest.Response("ID=" + id));
        }
    }

    @Test
    public void queryParameter() throws Exception {
        this.mvc.perform(get(this.getPath("/query?name=value")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("value")));
    }

    public static class ServiceQueryParameter {
        public void service(@HttpQueryParameter("name") String name, ObjectResponse<OfficeFloorSpecificTest.Response> response) {
            response.send(new OfficeFloorSpecificTest.Response(name));
        }
    }

    @Test
    public void header() throws Exception {
        this.mvc.perform(get(this.getPath("/header")).header("header", "VALUE").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("VALUE")));
    }

    public static class ServiceHeader {
        public void service(@HttpHeaderParameter("header") String header, ObjectResponse<OfficeFloorSpecificTest.Response> response) {
            response.send(new OfficeFloorSpecificTest.Response(header));
        }
    }

    @Test
    public void httpObject() throws Exception {
        this.mvc.perform(post(this.getPath("/users")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RequestEntity("ENTITY"))))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("ENTITY")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestEntity {
        private String request;
    }

    public static class ServiceObject {
        public void service(@HttpObject RequestEntity entity, ObjectResponse<OfficeFloorSpecificTest.Response> response) {
            response.send(new OfficeFloorSpecificTest.Response(entity.request));
        }
    }

    @Test
    public void httpServletRequest() throws Exception {
        this.mvc.perform(get(this.getPath("/httpServletRequest?name=Servlet")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    public static class SpringHttpServletRequest {
        public void service(HttpServletRequest request, ObjectResponse<String> response) {
            response.send(request.getParameter("name"));
        }
    }

    @Test
    public void httpServletResponse() throws Exception {
        this.mvc.perform(get("/spring/httpServletResponse"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    public static class SpringHttpServletResponse {
        public void service(HttpServletResponse response) throws IOException {
            response.getWriter().write("Servlet");
        }
    }

}
