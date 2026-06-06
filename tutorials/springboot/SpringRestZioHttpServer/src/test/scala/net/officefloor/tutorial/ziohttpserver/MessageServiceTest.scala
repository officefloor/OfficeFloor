package net.officefloor.tutorial.ziohttpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import zio.{FiberRefs, Runtime, RuntimeFlags, Unsafe, ZEnvironment}

// START SNIPPET: tutorial
class MessageServiceTest {

  @Test
  def retrieveMessage(): Unit = {
    val retrieve = for {
      m <- MessageService.getMessage(1)
    } yield m
    val message = Unsafe.unsafe { implicit unsafe =>
      runtime(1, "Hello World").unsafe.run(retrieve).getOrThrowFiberFailure()
    }
    assertEquals("Hello World", message.getContent)
  }

  def runtime(id: Int, content: String): Runtime[MessageRepository] =
    Runtime(ZEnvironment[MessageRepository](new TestMessageRepository(id, content)), FiberRefs.empty, RuntimeFlags.default)
}
// END SNIPPET: tutorial
