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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.http.Authorization;
import twitter4j.http.AuthorizationFactory;

/**
 * Connection to Twitter.
 * 
 * @author Daniel Sagenschneider
 */
public class TwitterConnection {

	/**
	 * Prefix text of distribution configuration tweet.
	 */
	private static final String DISTRIBUTE_PREFIX_TEXT = "distributed - ";

	/**
	 * {@link Twitter}.
	 */
	private final Twitter twitter;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link TwitterDistributeProperties}.
	 * @throws Exception
	 *             If fails to establish connection.
	 */
	public TwitterConnection(TwitterDistributeProperties properties)
			throws Exception {

		// Obtain the authorization
		Authorization authorisation = AuthorizationFactory
				.getInstance(new PropertyConfiguration(properties));

		// Create the twitter configured for connecting
		this.twitter = new TwitterFactory().getInstance(authorisation);
	}

	/**
	 * Obtains the tweets to distribute.
	 * 
	 * @return Listing of tweets.
	 * @throws TwitterException
	 *             If fails to obtain tweets.
	 */
	public List<Status> getTweetsToDistribute() throws TwitterException {

		// Obtain all tweets
		ResponseList<Status> tweets = this.twitter.getUserTimeline();

		// Obtain the list of tweets to distribute
		List<Status> potential = new LinkedList<Status>();
		Set<String> tweetIds = new HashSet<String>();
		for (Status tweet : tweets) {

			// Determine if distribute confirmation tweet
			String text = tweet.getText();
			if (text.startsWith(DISTRIBUTE_PREFIX_TEXT)) {
				// Add the distribution tweet id
				String tweetId = text
						.substring(DISTRIBUTE_PREFIX_TEXT.length());
				tweetIds.add(tweetId.trim());
				continue;
			}

			// Not distribution configuration tweet, so include for distribution
			potential.add(tweet);
		}

		// Filter out any already distributed tweets
		List<Status> tweetsToDistribute = new LinkedList<Status>();
		for (Status tweet : potential) {

			// Determine if already distributed tweet
			String tweetId = String.valueOf(tweet.getId());
			if (tweetIds.contains(tweetId)) {
				continue; // ignore as already distributed
			}

			// Add tweet to distribute
			tweetsToDistribute.add(tweet);
		}

		// Return the tweets to distribute
		return tweetsToDistribute;
	}

	/**
	 * Flags the tweet as distributed.
	 * 
	 * @param tweet
	 *            Tweet to flag as distributed.
	 * @throws TwitterException
	 *             If fails to flag tweet distributed.
	 */
	public void flagTweetDistributed(Status tweet) throws TwitterException {
		this.twitter.updateStatus(DISTRIBUTE_PREFIX_TEXT + tweet.getId());
	}

}