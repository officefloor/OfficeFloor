/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.tutorial.javascriptapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Tests the JavaScript application.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptAppTest extends TestCase {

	/**
	 * Allow running as application to manually test the JavaScript.
	 */
	public static void main(String[] arguments) throws Exception {
		OfficeFloorMain.open();
		System.out.println("Press [enter] to exit");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		OfficeFloorMain.close();
	}

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void setUp() throws Exception {
		OfficeFloorMain.open();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			this.client.close();
		} finally {
			OfficeFloorMain.close();
		}
	}

	// START SNIPPET: tutorial
	public void testHttpParameters() throws IOException {
		String response = this.doAjax("addition", "numberOne=2&numberTwo=1");
		assertEquals("Incorrect response", "3", response);
	}

	public void testHttpJson() throws IOException {
		String response = this.doAjax("subtraction", "{ \"numberOne\" : \"3\", \"numberTwo\" : \"1\" }");
		assertEquals("Incorrect response", "{\"result\":\"2\"}", response);
	}

	private String doAjax(String link, String payload) throws IOException {
		HttpPost post = new HttpPost("http://localhost:7878/template-" + link + ".woof");
		post.setEntity(new StringEntity(payload));
		HttpResponse response = this.client.execute(post);
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		return EntityUtils.toString(response.getEntity());
	}
	// END SNIPPET: tutorial

}