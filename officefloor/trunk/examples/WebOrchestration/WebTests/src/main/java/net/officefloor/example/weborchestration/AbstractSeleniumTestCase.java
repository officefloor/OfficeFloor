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
package net.officefloor.example.weborchestration;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Selenium {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSeleniumTestCase extends TestCase {

	/**
	 * Shirt {@link Product} name.
	 */
	public static final String PRODUCT_SHIRT = "Shirt";

	/**
	 * Trousers {@link Product} name.
	 */
	public static final String PRODUCT_TROUSERS = "Trousers";

	/**
	 * Hat {@link Product} name.
	 */
	public static final String PRODUCT_HAT = "Hat";

	/**
	 * {@link Product} identifiers.
	 */
	private static final Map<String, Long> productIdentifiers = new HashMap<String, Long>();

	/**
	 * URL file extension.
	 */
	public static String urlFileExtension = ".action";

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
	private long pageLoadWaitTime = 15000;

	/**
	 * Starting URL.
	 */
	private final String startingUrl;

	/**
	 * Initiate with starting URL - /.
	 */
	public AbstractSeleniumTestCase() {
		this("/");
	}

	/**
	 * Initiate.
	 * 
	 * @param startingUrl
	 *            Starting URL.
	 */
	public AbstractSeleniumTestCase(String startingUrl) {
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

	/**
	 * Aligns the state of this test to the server state.
	 */
	public void alignTestState() {
		DefaultSelenium initiate = createSeleniumClient();
		initiate.start();
		initiate.setTimeout(String.valueOf(this.pageLoadWaitTime));

		try {
			initiate.open("/initialState" + urlFileExtension);

			// Load the customer details
			customerName = initiate.getText("id=name");
			customerEmail = initiate.getText("id=email");

			// Load fresh set of products
			productIdentifiers.clear();
			this.loadProduct(PRODUCT_SHIRT, initiate);
			this.loadProduct(PRODUCT_TROUSERS, initiate);
			this.loadProduct(PRODUCT_HAT, initiate);

		} finally {
			initiate.stop();
		}

		// Indicated loaded initial state
		System.out.println("Initial State");
		System.out.println("\tCUSTOMER");
		System.out.println("\t\tname: " + customerName + ", email: "
				+ customerEmail);
		System.out.println("\tPRODUCTS");
		for (String productName : productIdentifiers.keySet()) {
			System.out.println("\t\tproduct: " + productName + " (id="
					+ productIdentifiers.get(productName) + ")");
		}
	}

	/*
	 * ==================== TestCase =========================
	 */

	@Override
	protected void setUp() throws Exception {

		// Indicate test being run
		System.out.println("=============================================");
		System.out.println("  VERIFY TEST: " + this.getName());
		System.out.println("=============================================");

		// Lazy load the setup state
		if ((customerName == null) || (customerName.trim().length() == 0)) {
			this.alignTestState();
		}

		// Start selenium client
		this.selenium = createSeleniumClient();
		this.selenium.start();
		this.selenium.setTimeout(String.valueOf(this.pageLoadWaitTime));

		// Reset for next test
		this.selenium.open("/testReset" + urlFileExtension);

		// Open the starting URL
		System.out.println("OPEN: " + this.startingUrl);
		this.selenium.open(this.startingUrl);

		// Ensure not logged in
		this.assertTextPresent("Hello(\\s+)Welcome");
	}

	/**
	 * Loads the {@link Product}.
	 */
	private void loadProduct(String productName, DefaultSelenium initiate) {
		String productId = initiate.getText("id=product-" + productName);
		productIdentifiers.put(productName, Long.valueOf(productId));
	}

	@Override
	public void runBare() throws Throwable {
		// Log failures for verification tests
		try {
			super.runBare();
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop selenium client
		this.selenium.stop();
	}

	/**
	 * Logs in.
	 */
	public void login() {
		// Ensure on index and not logged in
		this.assertTextPresent("Hello(\\s+)Welcome");

		// Login (expecting to start from index page)
		this.clickLink("Login");
		this.inputText("email", this.getCustomerEmail());
		this.inputText("password", "password");
		this.submit("loginCustomer");
	}

	/**
	 * Sets the time to wait on loading a page before timing out.
	 * 
	 * @param pageLoadWaitTime
	 *            Time to wait on loading a page before timing out.
	 */
	public void setLoadPageWaitTime(long pageLoadWaitTime) {
		this.pageLoadWaitTime = pageLoadWaitTime;
		if (this.selenium != null) {
			this.selenium.setTimeout(String.valueOf(this.pageLoadWaitTime));
		}
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

		// Log the click
		System.out.println("CLICK: " + link);

		// Click on the link
		this.selenium.click("link=" + link);
		this.waitForPageToLoad();

		// Log page received
		System.out.println("RESPONSE PAGE:");
		System.out.println(this.selenium.getHtmlSource());
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
		// Asset content available
		assertTrue("Expecting text: " + regexp, this.selenium
				.isTextPresent("regexp:" + regexp));
	}

	/**
	 * Assets that the table cell value is as expected.
	 * 
	 * @param tableIdentifier
	 *            Identifier of the table.
	 * @param row
	 *            Index of row containing the cell.
	 * @param column
	 *            Index of column containing the cell.
	 * @param expectedCellValue
	 *            Expected value for the cell.
	 */
	public void assertTableCellValue(String tableIdentifier, int row,
			int column, String expectedCellValue) {
		assertEquals("Incorrect value for cell " + row + "," + column
				+ " of table " + tableIdentifier, expectedCellValue,
				this.selenium.getTable(tableIdentifier + "." + row + "."
						+ column));
	}

	/**
	 * Submits form.
	 * 
	 * @param identifier
	 *            Identifier of submit input to submit form.
	 */
	public void submit(String identifier) {

		// Log submit
		System.out.println("SUBMIT: " + identifier);

		// Submit
		this.selenium.click(identifier);
		this.waitForPageToLoad();

		// Log page received
		System.out.println("RESPONSE PAGE:");
		System.out.println(this.selenium.getHtmlSource());
	}

}