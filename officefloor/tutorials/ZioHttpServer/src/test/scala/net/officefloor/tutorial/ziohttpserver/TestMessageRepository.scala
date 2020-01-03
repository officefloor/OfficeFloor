/*-
 * #%L
 * ZIO Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
}
