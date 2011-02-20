/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.twitter.publish;

import net.officefloor.twitter.OAuthVerifier;
import net.officefloor.twitter.PublishContext;
import net.officefloor.twitter.Publisher;
import oauth.signpost.OAuthConsumer;

import org.apache.http.client.methods.HttpPost;

/**
 * {@link Publisher} for Digg.
 * 
 * @author Daniel Sagenschneider
 */
public class Digg implements Publisher {

	@Override
	public void publish(String message, PublishContext context)
			throws Exception {

		// Create the consumer
		OAuthConsumer consumer = context.createOAuthConsumer("digg",
				"http://services.digg.com/oauth/request_token",
				"http://services.digg.com/oauth/access_token",
				"http://digg.com/oauth/authenticate", new OAuthVerifier() {
					@Override
					public String verify(String url, OAuthConsumer consumer,
							PublishContext context) throws Exception {
						// Request access
						context.connectToWebServer(url);

						// Login to grant access
						context.inputWebPageText("ident", "sagenschneider");
						context.inputWebPageText("password",
								context.getProperty("digg.password"));
						context.submitWebPageForm("submit");

						// Obtain and return the verifier
						String verifier = context
								.getWebPageText("//p[@class='auth-key']");
						return verifier;
					}
				});

		// Post comment
		HttpPost comment = context.createHttpPost(
				"http://services.digg.com/2.0/comment.post", "story_id",
				"20110214134256:649138b3-8090-4a57-809e-ff375f7cf427",
				"comment_text", message);
		context.doRequest(comment, consumer, 200);
	}

}