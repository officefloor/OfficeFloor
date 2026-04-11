package net.officefloor.spring.starter.rest.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ExceptionHandlingTutorialTest {

    @Autowired
    protected MockMvc mvc;

    @Test
    public void method_exception_handling() throws Exception {
        this.mvc.perform(get("/exception/method"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"Method handled: thrown\""));
    }

    @Test
    public void composition_exception_handling() throws Exception {
        this.mvc.perform(get("/exception/composition"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"Composition handled: thrown\""));
    }

    @Test
    public void spring_controller_advice() throws Exception {
        this.mvc.perform(get("/exception/spring"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Spring handled: thrown"));
    }
}
