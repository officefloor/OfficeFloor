/*-
 * #%L
 * OfficeFloor AppEngine Emulator
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.appengine.emulator;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * As AppEngine development server does NOT support HTTPS, this filter mimics
 * AppEngine running on HTTPS.
 * 
 * @author Daniel Sagenschneider
 */
public class AppEngineSecureFilter implements Filter {

	/*
	 * ========================== Filter =============================
	 */

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		// Indicate registering
		filterConfig.getServletContext().log("Initialising " + AppEngineSecureFilter.class.getSimpleName()
				+ " for AppEngine emulator to support HTTPS");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Flag as secure
		chain.doFilter(new MockSecureHttpServletRequest((HttpServletRequest) request), response);
	}

	@Override
	public void destroy() {
	}

	/**
	 * Secure {@link HttpServletRequest}.
	 */
	public static class MockSecureHttpServletRequest extends HttpServletRequestWrapper {

		/**
		 * Instantiate.
		 * 
		 * @param request {@link HttpServletRequest}.
		 */
		public MockSecureHttpServletRequest(HttpServletRequest request) {
			super(request);
		}

		/*
		 * ======================== HttpServletRequest =====================
		 */

		@Override
		public boolean isSecure() {
			return true;
		}
	}

}
