package net.officefloor.server.http.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpRequestCookies;

/**
 * Materialising {@link HttpRequestCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequestCookiesTest extends OfficeFrameTestCase {

	/**
	 * No cookie.
	 */
	public void testNoCookie() {
		assertCookies(null);
	}

	/**
	 * Ensure can parse out a single Cookie.
	 */
	public void testSingleCookie() {
		assertCookies("name=value", "name", "value");
	}

	/**
	 * Ensure can parse out multiple Cookies.
	 */
	public void testMultipleCookies() {
		assertCookies("one=1;two=2;three=3", "three", "3", "two", "2", "one", "1");
	}

	/**
	 * Ensure can parse out a Cookie with quoted value.
	 */
	public void testQuotedCookie() {
		assertCookies("name=\"quoted\"", "name", "quoted");
	}

	/**
	 * Ignore Cookie without name or value.
	 */
	public void testIgnoreBlankCookie() {
		assertCookies("; ; = ; ");
	}

	/**
	 * Ignore Cookie with only a name.
	 */
	public void testIgnoreNameOnlyCookie() {
		assertCookies("ignore;name");
	}

	/**
	 * Ensure can parse out Cookie with extra spacing.
	 */
	public void testExtraSpacingCookie() {
		assertCookies("  name  =  value   ;  ", "name", "value");
	}

	/**
	 * Asserts the Cookies.
	 * 
	 * @param cookieString
	 *            Cookie {@link HttpHeader} value.
	 * @param cookieNameValuePairs
	 *            Expected Cookie name/value pairs.
	 */
	private static void assertCookies(String cookieString, String... cookieNameValuePairs) {
		HttpRequestCookies cookies = cookies(cookieString);

		// Ensure correct number of cookies
		assertEquals("Incorrect number of cookies", cookieNameValuePairs.length / 2, cookies.length());

		// Ensure correct cookies returned
		Iterator<HttpRequestCookie> iterator = cookies.iterator();
		for (int i = 0; i < cookieNameValuePairs.length; i += 2) {
			String name = cookieNameValuePairs[i];
			String value = cookieNameValuePairs[i + 1];

			// Ensure have cookie value
			assertTrue("Should have cookie (" + name + ")", iterator.hasNext());
			HttpRequestCookie cookie = iterator.next();
			assertEquals("Incorrect cookie name", name, cookie.getName());
			assertEquals("Incorrect cookie value", value, cookie.getValue());

			// Ensure also able to obtain cookie by name
			HttpRequestCookie found = cookies.getCookie(name);
			assertEquals("Incorrect found cookie name", name, found.getName());
			assertEquals("Incorrect found cookie value", value, found.getValue());

			// Ensure able to obtain cookie at index
			HttpRequestCookie index = cookies.cookieAt(i / 2);
			assertEquals("Incorrect indexed cookie name", name, index.getName());
			assertEquals("Incorrect indexed cookie value", value, index.getValue());
		}
		assertFalse("Should have no further cookies", iterator.hasNext());
	}

	/**
	 * Creates the {@link HttpRequestCookies}.
	 * 
	 * @param cookieString
	 *            Cookie string. May be <code>null</code> for no Cookie
	 *            {@link HttpHeader}.
	 * @return {@link HttpRequestCookies}.
	 */
	private static HttpRequestCookies cookies(String cookieString) {

		// Create the materialising HTTP headers
		NonMaterialisedHttpHeaders headers = new NonMaterialisedHttpHeaders() {

			@Override
			public Iterator<NonMaterialisedHttpHeader> iterator() {
				List<NonMaterialisedHttpHeader> headers = new ArrayList<>(1);
				if (cookieString != null) {
					headers.add(new NonMaterialisedHttpHeader() {

						@Override
						public CharSequence getName() {
							return "Cookie";
						}

						@Override
						public HttpHeader materialiseHttpHeader() {
							return new HttpHeader() {

								@Override
								public String getName() {
									return "Cookie";
								}

								@Override
								public String getValue() {
									return cookieString;
								}
							};
						}
					});
				}
				return headers.iterator();
			}

			@Override
			public int length() {
				return (cookieString == null) ? 0 : 1;
			}
		};

		// Return the request cookies
		return new MaterialisingHttpRequestCookies(new MaterialisingHttpRequestHeaders(headers));
	}

}