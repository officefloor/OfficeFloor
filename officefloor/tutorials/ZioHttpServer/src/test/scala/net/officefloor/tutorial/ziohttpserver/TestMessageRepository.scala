package net.officefloor.tutorial.ziohttpserver

import java.lang
import java.util.Optional

/**
 * Test {@link TestMessageRepository}.
 */
class TestMessageRepository(id: Int, content: String) extends MessageRepository {

  override def findById(id: Integer): Optional[Message] =
    Optional.of(new Message(id, content))

  /*
   * ================== Unused methods =======================
   */

  override def save[S <: Message](entity: S): S = throw new IllegalStateException("Not used")

  override def saveAll[S <: Message](entities: lang.Iterable[S]): lang.Iterable[S] = throw new IllegalStateException("Not used")

  override def existsById(id: Integer): Boolean = throw new IllegalStateException("Not used")

  override def findAll(): lang.Iterable[Message] = throw new IllegalStateException("Not used")

  override def findAllById(ids: lang.Iterable[Integer]): lang.Iterable[Message] = throw new IllegalStateException("Not used")

  override def count(): Long = throw new IllegalStateException("Not used")

  override def deleteById(id: Integer): Unit = throw new IllegalStateException("Not used")

  override def delete(entity: Message): Unit = throw new IllegalStateException("Not used")

  override def deleteAll(entities: lang.Iterable[_ <: Message]): Unit = throw new IllegalStateException("Not used")

  override def deleteAll(): Unit = throw new IllegalStateException("Not used")

  override def deleteAllById(iterable: lang.Iterable[_ <: Integer]): Unit = throw new IllegalStateException("Not used")
}