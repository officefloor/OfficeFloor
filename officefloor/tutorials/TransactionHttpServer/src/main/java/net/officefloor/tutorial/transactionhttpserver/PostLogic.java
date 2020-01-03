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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.ObjectResponse;

/**
 * Logic for the post page.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class PostLogic {

	public void getPosts(PostRepository repository, ObjectResponse<List<Post>> responder) {
		List<Post> posts = new LinkedList<>();
		for (Post post : repository.findAll()) {
			posts.add(post);
		}
		responder.send(posts);
	}

	public void create(Post post, PostRepository repository, ObjectResponse<Post> responder) {
		repository.save(post);
		responder.send(post);
	}

}
// END SNIPPET: tutorial
