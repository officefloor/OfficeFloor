package net.officefloor.woof;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link SectionSource} to test adding profiles.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProfileSectionSource extends AbstractSectionSource {

	/*
	 * ===================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Add the servicing function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("function",
				new MockPropertyFunctionSource());
		SectionFunction function = namespace.addSectionFunction("function", "function");

		// Provide dependencies
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		designer.link(function.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);

		// Link servicing
		SectionInput input = designer.addSectionInput("service", null);
		designer.link(input, function);
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link ManagedFunctionSource} to test overriding a {@link Property}.
	 */
	private static class MockPropertyFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * =============== ManagedFunctionSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the profiles
			String profiles = String.join(",", context.getProfiles());

			// Provide function to respond with property value
			functionNamespaceTypeBuilder.addManagedFunctionType("function", () -> (mfContext) -> {
				ServerHttpConnection connection = (ServerHttpConnection) mfContext
						.getObject(Dependencies.SERVER_HTTP_CONNECTION);
				connection.getResponse().getEntityWriter().write(profiles);
			}, Dependencies.class, None.class).addObject(ServerHttpConnection.class)
					.setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}