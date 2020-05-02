package net.officefloor.jaxrs;

import java.util.Properties;

import org.glassfish.jersey.servlet.ServletContainer;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * JAX-RS {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSupplierSource extends AbstractSupplierSource {

	/**
	 * ==================== SupplierSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Obtain the servlet manager
		ServletManager servletManager = ServletSupplierSource.getServletManager();

		// Add the JAX-RS servlet
		Properties properties = context.getProperties();
		servletManager.addServlet("JAXRS", ServletContainer.class, (servlet) -> {
			for (String name : properties.stringPropertyNames()) {
				String value = properties.getProperty(name);
				servlet.addInitParameter(name, value);
			}
		});
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

}