package net.officefloor.spring.webmvc.procedure;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link DispatcherServlet} for the {@link SpringControllerProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureDispatcherServlet extends DispatcherServlet {

	/**
	 * Attribute name for the {@link HandlerExecutionChain}.
	 */
	public static final String ATTRIBUTE_HANDLER_EXECUTION_CHAIN = "officefloor.procedure.handler";

	/**
	 * Serialise version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link WebApplicationContext}.
	 */
	@Dependency
	private WebApplicationContext injectedWebApplicationContext;

	/*
	 * ==================== DispatcherServlet =========================
	 */

	@Override
	protected WebApplicationContext initWebApplicationContext() {

		// Refresh based on context
		this.onRefresh(this.injectedWebApplicationContext);

		// Return the context
		return this.injectedWebApplicationContext;
	}

	@Override
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		return (HandlerExecutionChain) request.getAttribute(ATTRIBUTE_HANDLER_EXECUTION_CHAIN);
	}

}