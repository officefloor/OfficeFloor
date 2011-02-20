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

import junit.framework.TestCase;
import net.officefloor.twitter.publish.Blogspot;

/**
 * Tests the {@link TwitterConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class TwitterDistributorIT extends TestCase {

	/**
	 * Trigger from continuous build to distrubte tweets.
	 */
	public void testDistributeTweets() throws Exception {
		TwitterDistributor.main();
	}

	/*
	 * ================== Individual publisher testing ======================
	 */

	private Publisher publisher = new Blogspot();

	/**
	 * Allows for testing a publisher.
	 */
	public void _testPublisher() throws Exception {
		TwitterDistributeProperties properties = new TwitterDistributeProperties();
		String message = "test-" + Math.random();
		System.out.println("Publishing message: " + message);
		TwitterDistributor.publish(message, this.publisher, properties);
	}

}