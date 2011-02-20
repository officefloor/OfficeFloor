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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Context for {@link Publisher}.
 * 
 * @author Daniel Sagenschneider
 */
public class PublishContext {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client;

	/**
	 * {@link TwitterDistributeProperties}.
	 */
	private final TwitterDistributeProperties properties;

	/**
	 * {@link DefaultSelenium}.
	 */
	private DefaultSelenium selenium = null;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link TwitterDistributeProperties}.
	 */
	public PublishContext(TwitterDistributeProperties properties) {
		this.properties = properties;

		// Create the HTTP Client
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUseExpectContinue(params, false);
		this.client = new DefaultHttpClient(params);
	}

	/**
	 * Closes the context.
	 */
	protected void close() {
		this.client.getConnectionManager().shutdown();
		this.stopSelenium();
	}

	/**
	 * Stops {@link Selenium}.
	 */
	private void stopSelenium() {
		if (this.selenium != null) {
			this.selenium.stop();
		}
	}

	/**
	 * Obtains the property value.
	 * 
	 * @param name
	 *            Name of property.
	 * @return Property value.
	 */
	public String getProperty(String name) {
		return this.properties.getEnsuredProperty(name);
	}

	/**
	 * Connect to web server.
	 * 
	 * @param url
	 *            URL.
	 */
	public void connectToWebServer(String url) {
		this.stopSelenium(); // ensure previous stopped if new connection
		System.out.println("Connecting to " + url);
		this.selenium = new DefaultSelenium("localhost", 4444, "*firefox", url);
		this.selenium.start();
		this.selenium.setTimeout("15000");
		this.selenium.open(url);
	}

	/**
	 * Opens the web page on the connected Web Server.
	 * 
	 * @param uri
	 *            URI of the web page.
	 */
	public void openWebPage(String uri) {
		this.selenium.open(uri);
	}

	/**
	 * Waits for the page to load.
	 */
	public void waitForWebPageToLoad() {
		this.selenium.waitForPageToLoad("15000");
	}

	/**
	 * Clicks the link and waits for linked page to load.
	 * 
	 * @param link
	 *            Link to click.
	 */
	public void clickWebPageLink(String link) {

		// Log the click
		System.out.println("CLICK: " + link);

		// Click on the link
		this.selenium.click("link=" + link);
		this.waitForWebPageToLoad();

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
	public void inputWebPageText(String identifier, String text) {

		// Log the input
		System.out.println("INPUT: " + identifier + "=" + text);

		// Input the text
		this.selenium.type(identifier, text);
	}

	/**
	 * Submits form.
	 * 
	 * @param identifier
	 *            Identifier of submit input to submit form.
	 */
	public void submitWebPageForm(String identifier) {

		// Log submit
		System.out.println("SUBMIT: " + identifier);

		// Submit
		this.selenium.click(identifier);
		this.waitForWebPageToLoad();

		// Log page received
		System.out.println("RESPONSE PAGE:");
		System.out.println(this.selenium.getHtmlSource());
	}

	/**
	 * Obtains the text from the element identified by the xpath.
	 * 
	 * @param xpath
	 *            XPath to the element.
	 * @return Text for the identified element.
	 */
	public String getWebPageText(String xpath) {
		return this.selenium.getText("xpath=" + xpath);
	}

	/**
	 * Asserts that the text regular expression is present.
	 * 
	 * @param regexp
	 *            Text regular expression.
	 */
	public void assertWebPageTextPresent(String regexp) {

		// Log assertion
		System.out.println("ASSERT: " + regexp);

		// Asset content available
		TestCase.assertTrue("Expecting text: " + regexp,
				this.selenium.isTextPresent("regexp:" + regexp));
	}

	/**
	 * Creates the {@link HttpPost} request.
	 * 
	 * @param url
	 *            URL.
	 * @param nameValuePairs
	 *            Name value pairs.
	 * @return {@link HttpPost}.
	 * @throws Exception
	 *             If fails to create {@link HttpPost}.
	 */
	public HttpPost createHttpPost(String url, String... nameValuePairs)
			throws Exception {
		HttpPost post = new HttpPost(url);
		List<BasicNameValuePair> entries = new LinkedList<BasicNameValuePair>();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			BasicNameValuePair entry = new BasicNameValuePair(name, value);
			entries.add(entry);
		}
		post.setEntity(new UrlEncodedFormEntity(entries, HTTP.UTF_8));
		return post;
	}

	/**
	 * Undertakes the request.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @param expectedStatus
	 *            Expected status code.
	 * @return Body of response.
	 * @throws IOException
	 *             If fails to undertake the request.
	 */
	public String doRequest(HttpUriRequest request, int expectedStatus)
			throws IOException {

		// Send request
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("REQUEST " + request.getRequestLine().getUri());
		HttpResponse response = this.client.execute(request);

		// Validate and log response
		System.out.println("RESPONSE "
				+ response.getStatusLine().getStatusCode() + " : "
				+ response.getStatusLine().getReasonPhrase());
		for (Header header : response.getAllHeaders()) {
			System.out.println("   " + header.getName() + ": "
					+ header.getValue());
		}

		// Obtain response
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		response.getEntity().writeTo(output);
		String responseBody = output.toString("UTF-8");
		System.out.println(responseBody);

		// Ensure successful
		TestCase.assertEquals("Must be successful", expectedStatus, response
				.getStatusLine().getStatusCode());

		// Return the response body
		return responseBody;
	}

	/**
	 * Undertakes a request with OAuth security.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @param consumer
	 *            {@link OAuthConsumer}.
	 * @param expectedStatus
	 *            Expected status.
	 * @return Body of response.
	 * @throws Exception
	 *             If fails to do the request.
	 */
	public String doRequest(HttpUriRequest request, OAuthConsumer consumer,
			int expectedStatus) throws Exception {

		// Sign the request
		consumer.sign(request);

		// Do the request
		return this.doRequest(request, expectedStatus);
	}

	/**
	 * Creates {@link OAuthConsumer} from configuration.
	 * 
	 * @param securityDomain
	 *            Security domain name.
	 * @return {@link OAuthConsumer}.
	 * @throws Exception
	 *             If fails to authenticate.
	 */
	public OAuthConsumer createOAuthConsumer(final String securityDomain)
			throws Exception {
		return this.createOAuthConsumer(securityDomain, null, null, null, null);
	}

	/**
	 * Creates the {@link OAuthConsumer}.
	 * 
	 * @param securityDomain
	 *            Security domain name.
	 * @param requestUrl
	 *            Request URL.
	 * @param accessUrl
	 *            Access URL.
	 * @param authenticateUrl
	 *            Authenticate URL.
	 * @param verifier
	 *            {@link OAuthVerifier} to obtain OAuth verifier.
	 * @return {@link OAuthConsumer}.
	 * @throws Exception
	 *             If fails to create the {@link OAuthConsumer}.
	 */
	public OAuthConsumer createOAuthConsumer(String securityDomain,
			String requestUrl, String accessUrl, String authenticateUrl,
			OAuthVerifier verifier) throws Exception {

		// Create consumer
		String consumerKey = this.properties.getEnsuredProperty(securityDomain
				+ ".consumerKey");
		String consumerSecret = this.properties
				.getEnsuredProperty(securityDomain + ".consumerSecret");
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey,
				consumerSecret);

		// Determine if load directly from properties
		if (verifier == null) {
			String accessToken = this.properties
					.getEnsuredProperty(securityDomain + ".accessToken");
			String accessTokenSecret = this.properties
					.getEnsuredProperty(securityDomain + ".accessTokenSecret");
			consumer.setTokenWithSecret(accessToken, accessTokenSecret);
			return consumer; // fully configured
		}

		// Create provider
		OAuthProvider provider = new DefaultOAuthProvider(requestUrl,
				accessUrl, authenticateUrl);
		String url = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

		// Verify
		String verification = verifier.verify(url, consumer, this);
		System.out.println("VERIFIER=" + verifier);
		provider.retrieveAccessToken(consumer, verification);

		// Return the consumer
		return consumer;
	}

	/**
	 * Sends the email.
	 * 
	 * @param toAddress
	 *            To address.
	 * @param subject
	 *            Subject.
	 * @param content
	 *            Content.
	 * @throws Exception
	 *             If fails to send email.
	 */
	public void sendEmail(String toAddress, String subject, String content)
			throws Exception {

		// Create the session
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host",
				this.properties.getEnsuredProperty("mail.smtp.host"));
		Session session = Session.getDefaultInstance(properties);

		// Create the message
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(this.properties
				.getEnsuredProperty("mail.from.address")));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(
				toAddress));
		msg.setSubject(subject);
		msg.setContent(content, "text/plain");

		// Send the message
		Transport.send(msg);
	}

}