package net.officefloor.tutorial.ziohttpserver

import org.scalatest.flatspec.AnyFlatSpec
import zio.{FiberRefs, Runtime, RuntimeFlags, Unsafe, ZEnvironment}
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
    val message = Unsafe.unsafe { implicit unsafe =>
      runtime(1, "Hello World").unsafe.run(retrieve).getOrThrowFiberFailure()
    }
    assert("Hello World" == message.getContent)
  }

  def runtime(id: Int, content: String): Runtime[MessageRepository] =
    Runtime(ZEnvironment[MessageRepository](new TestMessageRepository(id, content)), FiberRefs.empty, RuntimeFlags.default)
}
// END SNIPPET: tutorial