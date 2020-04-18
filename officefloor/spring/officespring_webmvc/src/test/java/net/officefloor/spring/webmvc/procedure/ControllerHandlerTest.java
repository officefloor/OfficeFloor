package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.tutorial.springapp.Application;
import net.officefloor.tutorial.springapp.InjectController;
import net.officefloor.tutorial.springapp.SimpleController;

/**
 * Ensures able to find {@link HandlerExecutionChain} for {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
public class ControllerHandlerTest extends OfficeFrameTestCase {

	/**
	 * {@link SimpleController}.
	 */
	public void testSimple() throws Exception {
		this.doControllerHandlerTest(SimpleController.class, "simple");
	}

	/**
	 * {@link InjectController#inject()}.
	 */
	public void testInject() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "inject");
	}

	/**
	 * {@link InjectController#status()}.
	 */
	public void testStatus() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "status");
	}

	/**
	 * {@link InjectController#pathParam(String)}.
	 */
	public void testPathParam() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "pathParam");
	}

	/**
	 * {@link InjectController#requestParam(String)}.
	 */
	public void testQueryParam() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "requestParam");
	}

	/**
	 * {@link InjectController#header(String)}.
	 */
	public void testHeader() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "header");
	}

	/**
	 * {@link InjectController#post(String)}.
	 */
	public void testPost() throws Exception {
		this.doControllerHandlerTest(InjectController.class, "post");
	}

	/**
	 * Undertakes finding the {@link HandlerExecutionChain} for the
	 * {@link Controller}.
	 * 
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of {@link Controller} {@link Method}.
	 */
	private void doControllerHandlerTest(Class<?> controllerClass, String controllerMethodName) throws Exception {

		// Create the application
		ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class).run();
		try {

			// Obtain the class loader
			ClassLoader classLoader = this.getClass().getClassLoader();

			// Obtain the handler for the controller method
			HandlerExecutionChain handler = SpringControllerProcedureRegistry.getHandler(controllerClass,
					controllerMethodName, classLoader, context);
			String qualifiedMethodName = controllerClass.getName() + "#" + controllerMethodName;
			assertNotNull("Should have handler for " + qualifiedMethodName, handler);
			assertTrue(
					"Incorrect handler for " + qualifiedMethodName + " (Handler: "
							+ handler.getHandler().getClass().getName() + ")",
					handler.getHandler() instanceof HandlerMethod);
			assertSame("Incorrect handler method", controllerMethodName,
					((HandlerMethod) handler.getHandler()).getMethod().getName());

		} finally {
			context.close();
		}
	}

}