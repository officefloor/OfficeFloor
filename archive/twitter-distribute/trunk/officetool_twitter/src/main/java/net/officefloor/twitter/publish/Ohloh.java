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

import net.officefloor.twitter.PublishContext;
import net.officefloor.twitter.Publisher;

import org.apache.http.client.methods.HttpPost;

/**
 * {@link Publisher} for Ohloh.
 * 
 * @author Daniel Sagenschneider
 */
public class Ohloh implements Publisher {

	@Override
	public void publish(String message, PublishContext context)
			throws Exception {

		// Ensure publish to OfficeFloor journal
		message += " (#OfficeFloor)";

		// Login
		HttpPost loginRequest = context.createHttpPost(
				"https://www.ohloh.net/sessions", "login[login]",
				"sagenschneider", "login[password]",
				context.getProperty("ohloh.password"));
		context.doRequest(loginRequest, 302);

		// Add journal entry
		HttpPost journalRequest = context.createHttpPost(
				"https://www.ohloh.net/accounts/sagenschneider/messages",
				"message[content]", message);
		journalRequest.addHeader("Referer",
				"https://www.ohloh.net/accounts/sagenschneider/messages");
		context.doRequest(journalRequest, 302);
	}

}