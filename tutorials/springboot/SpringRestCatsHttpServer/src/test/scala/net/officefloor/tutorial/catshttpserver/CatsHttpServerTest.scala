package net.officefloor.tutorial.catshttpserver

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.fasterxml.jackson.databind.ObjectMapper
import doobie.util.transactor.Transactor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
class CatsHttpServerTest {

  @Autowired var mvc: MockMvc = _
  @Autowired var mapper: ObjectMapper = _
  @Autowired var transactor: Transactor[IO] = _

  @Test
  def repositoryTest(): Unit = {
    implicit val runtime: IORuntime = IORuntime.global
    implicit val xa: Transactor[IO] = transactor
    val message = MessageRepository.findById(1).unsafeRunSync()
    assertEquals("Hi via doobie", message.content)
  }

  @Test
  def httpServerTest(): Unit = {
    mvc.perform(post("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(CatsRequest(1)))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().json(mapper.writeValueAsString(CatsResponse("Hi via doobie and Cats"))))
  }
}
// END SNIPPET: tutorial
