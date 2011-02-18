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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

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
	 * {@link Twitter}.
	 */
	private final Twitter twitter;

	/**
	 * Initiate.
	 * 
	 * @throws Exception
	 *             If fails to establish connection.
	 */
	public TwitterConnection() throws Exception {

		// Load properties from user properties
		File propertiesFile = new File(System.getProperty("user.home"),
				"twitter-distribute.properties");
		if (!propertiesFile.isFile()) {
			throw new FileNotFoundException("Can not find properties file: "
					+ propertiesFile.getPath());
		}
		Properties properties = new Properties();
		properties.load(new FileReader(propertiesFile));

		// Obtain the authorization
		Authorization authorisation = AuthorizationFactory
				.getInstance(new PropertyConfiguration(properties));

		// Create the twitter configured for connecting
		this.twitter = new TwitterFactory().getInstance(authorisation);
	}

	/**
	 * Obtains the tweets.
	 * 
	 * @return Listing of tweets.
	 * @throws TwitterException
	 *             If fails to obtain tweets.
	 */
	public List<Status> getTweets() throws TwitterException {
		ResponseList<Status> tweets = this.twitter.getUserTimeline();
		return tweets;
	}

}