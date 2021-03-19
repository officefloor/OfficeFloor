/*-
 * #%L
 * Testing of HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http;

import org.apache.http.client.HttpClient;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for a {@link HttpClient} to {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientRule extends AbstractHttpClientJUnit<HttpClientRule> implements TestRule {

	/**
	 * Instantiate for non-secure (HTTP) connection to {@link HttpServer}.
	 */
	public HttpClientRule() {
	}

	/**
	 * Instantiate with flagging if secure (HTTPS) connection to {@link HttpServer}.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 */
	public HttpClientRule(boolean isSecure) {
		super(isSecure);
	}

	/**
	 * Instantiate indicating the server port.
	 * 
	 * @param isSecure Indicates if secure (HTTPS).
	 * @param port     Server port to connect.
	 */
	public HttpClientRule(boolean isSecure, int port) {
		super(isSecure, port);
	}

	/*
	 * =============== TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access to rule
				HttpClientRule rule = HttpClientRule.this;

				// Undertake test
				try {
					rule.openHttpClient();

					// Undertake test
					base.evaluate();

				} finally {
					rule.closeHttpClient();
				}
			}
		};
	}

}
