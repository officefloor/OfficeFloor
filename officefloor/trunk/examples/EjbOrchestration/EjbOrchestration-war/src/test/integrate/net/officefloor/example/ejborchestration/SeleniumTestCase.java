/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.example.ejborchestration;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

import junit.framework.TestCase;

/**
 * Selenium {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class SeleniumTestCase extends TestCase {

	/**
	 * {@link Selenium} client.
	 */
	protected final DefaultSelenium selenium = new DefaultSelenium("localhost",
			4444, "*firefox", "http://localhost:8080/");

	/**
	 * Time to wait for page to load.
	 */
	private long pageLoadWaitTime = 30000;

	/**
	 * Starting URL.
	 */
	private final String startingUrl;

	/**
	 * Initiate with starting URL - /.
	 */
	public SeleniumTestCase() {
		this("/");
	}

	/**
	 * Initiate.
	 * 
	 * @param startingUrl
	 *            Starting URL.
	 */
	public SeleniumTestCase(String startingUrl) {
		this.startingUrl = startingUrl;
	}

	/*
	 * ==================== TestCase =========================
	 */

	@Override
	protected void setUp() throws Exception {
		// Start selenium client
		this.selenium.start();

		// Open the starting URL
		this.selenium.open(this.startingUrl);
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop selenium client
		this.selenium.stop();
	}

	/**
	 * Waits for the page to load.
	 */
	public void waitForPageToLoad() {
		this.selenium.waitForPageToLoad(String.valueOf(this.pageLoadWaitTime));
	}

	/**
	 * Clicks the link and waits for linked page to load.
	 * 
	 * @param link
	 *            Link to click.
	 */
	public void clickLink(String link) {
		this.selenium.click("link=" + link);
		this.waitForPageToLoad();
	}

	/**
	 * Input text.
	 * 
	 * @param identifier
	 *            Identifier.
	 * @param text
	 *            Text to input.
	 */
	public void inputText(String identifier, String text) {
		this.selenium.type(identifier, text);
	}

	/**
	 * Asserts that the text regular expression is present.
	 * 
	 * @param regexp
	 *            Text regular expression.
	 */
	public void assertTextPresent(String regexp) {
		assertTrue("Expecting text: " + regexp, this.selenium
				.isTextPresent("regexp:" + regexp));
	}

	/**
	 * Submits form.
	 * 
	 * @param identifier
	 *            Identifier of submit input to submit form.
	 */
	public void submit(String identifier) {
		this.selenium.click(identifier);
		this.waitForPageToLoad();
	}

}