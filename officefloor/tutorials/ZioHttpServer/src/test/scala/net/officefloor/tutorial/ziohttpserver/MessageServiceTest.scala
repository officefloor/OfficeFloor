package net.officefloor.tutorial.ziohttpserver

import org.scalatest.flatspec.AnyFlatSpec
import zio.Runtime
import zio.internal.Platform

/**
 * Tests the {@link MessageService}.
 */
// START SNIPPET: tutorial
class MessageServiceTest extends AnyFlatSpec {

  it should "retrieve Message" in {
    val retrieve = for {
      m <- MessageService.getMessage(1)
    } yield m
    val message = runtime(1, "Hello World").unsafeRun(retrieve)
    assert("Hello World" == message.getContent)
  }

  def runtime(id: Int, content: String): Runtime[InjectMessageRepository] =
    Runtime(new InjectMessageRepository {
      override val messageRepository: MessageRepository = new TestMessageRepository(id, content)
    }, Platform.default)
}
// END SNIPPET: tutorial