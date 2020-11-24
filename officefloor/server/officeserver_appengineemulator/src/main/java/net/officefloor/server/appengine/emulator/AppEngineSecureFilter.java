/*-
 * #%L
 * OfficeFloor AppEngine Emulator
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
