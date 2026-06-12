package net.officefloor.tutorial.ziohttpserver

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
class ZioHttpServerTest {

  @Autowired var mvc: MockMvc = _
  @Autowired var mapper: ObjectMapper = _

  @Test
  def getMessage(): Unit = {
    mvc.perform(post("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(ZioRequest(1)))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().json(mapper.writeValueAsString(ZioResponse("Hi via ZIO"))))
  }
}
// END SNIPPET: tutorial
