/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.sessionhttpserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	@HttpSessionStateful
	public static class Posts implements Serializable {

		private final List<Post> posts = new ArrayList<Post>();

		public void addPost(Post post) {
			this.posts.add(post);
		}

		public Post[] getPosts() {
			return this.posts.toArray(new Post[this.posts.size()]);
		}
	}

	@Data
	@HttpParameters
	public static class Post implements Serializable {

		private String text;
	}

	/**
	 * Provides values from the HTTP session.
	 * 
	 * @param posts
	 *            {@link Posts} being the session bound object.
	 * @return {@link Posts} to contain values to render to page.
	 */
	public Posts getTemplateData(Posts posts) {
		return posts;
	}

	/**
	 * Handles the post form submission.
	 * 
	 * @param post
	 *            {@link Post} that is dependency injected with HTTP parameters
	 *            loaded onto it.
	 * @param posts
	 *            {@link Posts} that is dependency injected from the HTTP
	 *            session.
	 */
	public void post(Post post, Posts posts) {
		posts.addPost(post);
	}

}
// END SNIPPET: example