package net.officefloor.spring.starter.rest;

import net.officefloor.web.ObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorSpringBootTest {

    protected @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username= "User", roles = "USER")
    public void officefloor() throws Exception {
        this.mvc.perform(get("/officefloor").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("\"OfficeFloor\"")));
    }

    public static class Service {
        public void service(ObjectResponse<String> response) {
            response.send("OfficeFloor");
        }
    }

}
