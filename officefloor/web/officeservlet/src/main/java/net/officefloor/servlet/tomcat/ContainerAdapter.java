package net.officefloor.servlet.tomcat;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.CoyoteAdapter;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * Delegate {@link Connector} to provide to {@link CoyoteAdapter} to service via
 * {@link Container}.
 * 
 * @author Daniel Sagenschneider
 */
public class ContainerAdapter extends CoyoteAdapter {

	/**
	 * Instantiate.
	 * 
	 * @param container   {@link Container} to adapt.
	 * @param connector   {@link Connector}.
	 * @param classLoader {@link ClassLoader}.
	 */
	public ContainerAdapter(Container container, Connector connector, ClassLoader classLoader) {
		super(new ContainerConnector(container, connector, classLoader));
	}

	/**
	 * {@link Container} {@link Connector}.
	 */
	private static class ContainerConnector extends Connector {

		/**
		 * Delegate {@link Connector}.
		 */
		private final Connector delegate;

		/**
		 * Proxy {@link Service}.
		 */
		private final Service proxyService;

		/**
		 * Instantiate.
		 * 
		 * @param container   {@link Container}.
		 * @param delegate    {@link Connector}.
		 * @param classLoader {@link ClassLoader}.
		 */
		public ContainerConnector(Container container, Connector delegate, ClassLoader classLoader) {
			super(OfficeFloorProtocol.class.getName());
			this.delegate = delegate;

			// Override domain (as getter final)
			this.setDomain(this.delegate.getDomain());

			// Create proxy engine
			Engine proxyEngine = (Engine) Proxy.newProxyInstance(classLoader, new Class[] { Engine.class },
					(proxy, method, args) -> {
						if ("getPipeline".equals(method.getName())) {
							// Provide container pipeline to use
							return container.getPipeline();
						} else {
							// Delegate to engine
							Engine engine = this.delegate.getService().getContainer();
							Method actualMethod = engine.getClass().getMethod(method.getName(),
									method.getParameterTypes());
							return actualMethod.invoke(engine, args);
						}
					});

			// Create proxy service
			this.proxyService = (Service) Proxy.newProxyInstance(classLoader, new Class[] { Service.class },
					(proxy, method, args) -> {
						if ("getContainer".equals(method.getName())) {
							// Allow for container
							return proxyEngine;
						} else {
							// Delegate to service
							Service service = this.delegate.getService();
							Method actualMethod = service.getClass().getMethod(method.getName(),
									method.getParameterTypes());
							return actualMethod.invoke(service, args);
						}
					});
		}

		/*
		 * ====================== Connector =======================
		 */

		@Override
		public Service getService() {
			return this.proxyService;
		}

		/*
		 * ================= Connector (delegate) =================
		 */

		@Override
		public Request createRequest() {
			return this.delegate.createRequest();
		}

		@Override
		public Response createResponse() {
			return this.delegate.createResponse();
		}

		@Override
		public Charset getURICharset() {
			return this.delegate.getURICharset();
		}

		@Override
		public boolean getXpoweredBy() {
			return this.delegate.getXpoweredBy();
		}

		@Override
		public LifecycleState getState() {
			return this.delegate.getState();
		}

		@Override
		public String getScheme() {
			return this.delegate.getScheme();
		}

		@Override
		public boolean getSecure() {
			return this.delegate.getSecure();
		}

		@Override
		public String getProxyName() {
			return this.delegate.getProxyName();
		}

		@Override
		public int getProxyPort() {
			return this.delegate.getProxyPort();
		}

		@Override
		public boolean getAllowTrace() {
			return this.delegate.getAllowTrace();
		}

		@Override
		public boolean getUseIPVHosts() {
			return this.delegate.getUseIPVHosts();
		}
	}

}