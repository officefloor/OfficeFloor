package net.officefloor.tutorial.ziohttpserver

import org.scalatest.FlatSpec
import zio.Runtime
import zio.internal.PlatformLive

/**
 * Tests the {@link MessageService}.
 */
// START SNIPPET: tutorial
class MessageServiceTest extends FlatSpec {

  it should "retrieve Message" in {
    val retrieve = for {
      m <- MessageService.getMessage(1)
    } yield m
    val message = runtime(1, "Hello World").unsafeRun(retrieve)
    assert("Hello World" == message.getContent)
  }

  def runtime(id: Int, content: String): Runtime[InjectMessageRepository] =
    Runtime(new InjectMessageRepository {
      override val messageRepository: MessageRepository = new TestMessageRepository(1, "Hello World")
    }, PlatformLive.Default)
}
// END SNIPPET: tutorial