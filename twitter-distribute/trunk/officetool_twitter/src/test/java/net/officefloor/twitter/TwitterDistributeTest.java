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
package net.officefloor.twitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import twitter4j.Status;

/**
 * Tests the {@link TwitterConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class TwitterDistributeTest extends TestCase {

	/**
	 * {@link TwitterDistributeProperties}.
	 */
	private TwitterDistributeProperties properties;

	@Override
	protected void setUp() throws Exception {
		this.properties = new TwitterDistributeProperties();
	}

	/**
	 * Ensure able to distribute tweets.
	 */
	public void testDistributeTweets() throws Exception {

		// Obtains the tweets
		TwitterConnection connection = new TwitterConnection(this.properties);
		List<Status> tweets = connection.getTweetsToDistribute();

		// Indicate number of tweets to distribute
		System.out.println("Tweets to distribute " + tweets.size());

		// Reverse order so order is maintained as published sequentially
		Collections.reverse(tweets);

		// Distribute tweets
		for (Status tweet : tweets) {

			// Obtain message for distribution
			String message = tweet.getText();
			message = (message != null ? message.trim() : "");
			if (!(message.endsWith("."))) {
				message += "."; // ensure end with period
			}
			message += " More info at http://www.officefloor.net";

			// Indicate tweet being distributed
			System.out
					.println("===================================================================");
			System.out.println("TWEET-" + tweet.getId() + ": " + message);

			// Publish tweet for distribution
			this.publishToOhloh(message);

			// Flag tweet distributed
			connection.flagTweetDistributed(tweet);
			System.out.println("TWEET-" + tweet.getId() + " distributed");
		}
	}

	/**
	 * Publishes message to Ohloh.
	 * 
	 * @param message
	 *            Message to be published.
	 * @throws Exception
	 *             If fails to publish message.
	 */
	public void publishToOhloh(String message) throws Exception {

		// Ensure publish to OfficeFloor journal
		message += " (#OfficeFloor)";

		HttpClient client = new DefaultHttpClient();
		try {

			// Create login request
			HttpPost loginRequest = new HttpPost(
					"https://www.ohloh.net/sessions");
			loginRequest
					.setEntity(new UrlEncodedFormEntity(
							Arrays.asList(
									new BasicNameValuePair("login[login]",
											"sagenschneider"),
									new BasicNameValuePair(
											"login[password]",
											this.properties
													.getEnsuredProperty("ohloh.password"))),
							HTTP.UTF_8));

			// Login
			this.doRequest(loginRequest, client, 302);

			// Create add journal request
			HttpPost journalRequest = new HttpPost(
					"https://www.ohloh.net/accounts/sagenschneider/messages");
			journalRequest.addHeader("Referer",
					"https://www.ohloh.net/accounts/sagenschneider/messages");
			journalRequest.setEntity(new UrlEncodedFormEntity(
					Arrays.asList(new BasicNameValuePair("message[content]",
							message)), HTTP.UTF_8));

			// Create journal entry
			this.doRequest(journalRequest, client, 302);

		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Undertakes the request.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @param client
	 *            {@link HttpClient}.
	 * @param expectedStatus
	 *            Expected status code.
	 * @throws IOException
	 *             If fails to undertake the request.
	 */
	private void doRequest(HttpUriRequest request, HttpClient client,
			int expectedStatus) throws IOException {

		// Send request
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("REQUEST " + request.getRequestLine().getUri());
		HttpResponse response = client.execute(request);

		// Validate and log response
		System.out.println("RESPONSE "
				+ response.getStatusLine().getStatusCode() + " : "
				+ response.getStatusLine().getReasonPhrase());
		for (Header header : response.getAllHeaders()) {
			System.out.println("   " + header.getName() + ": "
					+ header.getValue());
		}
		response.getEntity().writeTo(System.out);

		// Ensure successful
		assertEquals("Must be successful", expectedStatus, response
				.getStatusLine().getStatusCode());
	}

}