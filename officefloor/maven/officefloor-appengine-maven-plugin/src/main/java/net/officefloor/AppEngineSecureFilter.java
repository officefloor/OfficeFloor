package net.officefloor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * As AppEngine development server does NOT support HTTPS, this filter mimics
 * AppEngine running on HTTPS.
 * 
 * @author Daniel Sagenschneider
 */
@WebFilter("/*")
public class AppEngineSecureFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Flag as secure
		chain.doFilter(new MockSecureHttpServletRequest((HttpServletRequest) request), response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	public static class MockSecureHttpServletRequest extends HttpServletRequestWrapper {

		public MockSecureHttpServletRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public boolean isSecure() {
			return true;
		}
	}

}