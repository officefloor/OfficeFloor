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
	 * {@link Customer} name.
	 */
	private static String customerName;

	/**
	 * {@link Customer} email.
	 */
	private static String customerEmail;

	/**
	 * Creates the {@link DefaultSelenium} client.
	 * 
	 * @return {@link DefaultSelenium} client.
	 */
	private static DefaultSelenium createSeleniumClient() {
		return new DefaultSelenium("localhost", 4444, "*firefox",
				"http://localhost:8080/");
	}

	/**
	 * {@link Selenium} client.
	 */
	protected DefaultSelenium selenium;

	/**
	 * Time to wait for page to load.
	 */
	private long pageLoadWaitTime = 5000;

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

	/**
	 * Obtains the {@link Customer} email.
	 * 
	 * @return {@link Customer} email.
	 */
	public String getCustomerEmail() {
		return customerEmail;
	}

	/**
	 * Obtains the {@link Customer} name.
	 * 
	 * @return {@link Customer} name.
	 */
	public String getCustomerName() {
		return customerName;
	}

	/**
	 * Obtains the password for the {@link Customer}.
	 * 
	 * @return Password for the {@link Customer}.
	 */
	public String getCustomerPassword() {
		return "AnyPasswordWillDo";
	}

	/*
	 * ==================== TestCase =========================
	 */

	@Override
	protected void setUp() throws Exception {

		// Lazy load the setup state
		if (ActionUtil.isBlank(customerName)) {
			DefaultSelenium initiate = createSeleniumClient();
			initiate.start();
			try {
				initiate.open("/initialState.action");
				customerName = initiate.getText("id=name");
				customerEmail = initiate.getText("id=email");
			} finally {
				initiate.stop();
			}

			// Indicated loaded initial state
			System.out.println("Initial State\n" + "\tName: " + customerName
					+ "\n\tEmail: " + customerEmail);
		}

		// Start selenium client
		this.selenium = createSeleniumClient();
		this.selenium.start();

		// Open the starting URL
		this.selenium.open(this.startingUrl);
		
		// Ensure not logged in
		this.assertTextPresent("Hello(\\s+)Welcome");
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