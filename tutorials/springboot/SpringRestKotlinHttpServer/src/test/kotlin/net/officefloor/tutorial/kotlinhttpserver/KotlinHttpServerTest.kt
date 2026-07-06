package net.officefloor.tutorial.kotlinhttpserver

import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
class KotlinHttpServerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun service() {
        mvc.perform(post("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(KotlinRequest("Daniel"))))
                .andExpect(status().isOk)
                .andExpect(content().json(mapper.writeValueAsString(KotlinResponse("Hello Daniel from Kotlin"))))
    }
}
// END SNIPPET: tutorial
