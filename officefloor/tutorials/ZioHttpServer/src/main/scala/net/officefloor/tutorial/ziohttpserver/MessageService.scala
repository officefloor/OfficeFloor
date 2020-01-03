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

import zio.ZIO

/**
 * Message service.
 */
// START SNIPPET: tutorial
object MessageService {

  def getMessage(id: Int): ZIO[InjectMessageRepository, Throwable, Message] =
    ZIO.accessM(env => ZIO.effect(env.messageRepository findById id orElseThrow(() => new NoSuchElementException(s"No message by id $id"))))

}
// END SNIPPET: tutorial
