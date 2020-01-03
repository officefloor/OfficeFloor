/*-
 * #%L
 * Transaction HTTP Server Tutorial
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

package net.officefloor.tutorial.transactionhttpserver;

import java.io.EOFException;

import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Transaction logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TransactionLogic {

	public IllegalArgumentException rollback(Post post, PostRepository repository) {
		repository.save(post);
		return new IllegalArgumentException("rolled back");
	}

	public EOFException commit(Post post, PostRepository repository) throws EOFException {
		repository.save(post);
		return new EOFException("committed");
	}

	public void fail(@Parameter Exception failure, PostRepository repository, TeamMarkerBean marker) throws Exception {
		repository.save(new Post(null, "Additional"));
		throw failure;
	}

}
// END SNIPPET: tutorial
