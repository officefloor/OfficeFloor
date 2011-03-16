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

import java.util.Collections;
import java.util.List;

import net.officefloor.twitter.publish.Blogspot;
import net.officefloor.twitter.publish.Ohloh;
import net.officefloor.twitter.publish.WordPress;
import twitter4j.Status;

/**
 * Undertakes the twitter distribution.
 * 
 * @author Daniel Sagenschneider
 */
public class TwitterDistributor {

	/**
	 * {@link Publisher} instances.
	 */
	private static final Publisher[] PUBLISHERS = new Publisher[] {
			new Ohloh(), new Blogspot(), new WordPress() };

	/**
	 * Undertakes the distribution of tweets.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to distribute tweets.
	 */
	public static void main(String... args) throws Exception {

		// Obtain the properties
		TwitterDistributeProperties properties = new TwitterDistributeProperties();

		// Obtains the tweets
		TwitterConnection connection = new TwitterConnection(properties);
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
			for (Publisher publisher : PUBLISHERS) {
				publish(message, publisher, properties);
			}

			// Flag tweet distributed
			connection.flagTweetDistributed(tweet);
			System.out.println("TWEET-" + tweet.getId() + " distributed");
		}
	}

	/**
	 * Publishes the tweet.
	 * 
	 * @param message
	 *            Message of tweet.
	 * @param publisher
	 *            {@link Publisher}.
	 * @param properties
	 *            {@link TwitterDistributeProperties}.
	 * @throws Exception
	 *             If fails to publish tweet.
	 */
	public static void publish(String message, Publisher publisher,
			TwitterDistributeProperties properties) throws Exception {

		// Publish tweet
		PublishContext context = new PublishContext(properties);
		try {
			publisher.publish(message, context);
		} finally {
			context.close();
		}
	}

}