package net.officefloor.web.jwt.authority.combined;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;

/**
 * Handling of retrieving keys when the {@link JwtAuthority} is hosted in the
 * same server as the {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CombinedServerRetrieveValidateKeysSectionSource extends AbstractSectionSource {

	/**
	 * Name of the {@link SectionInput} to retrieve the {@link JwtValidateKey}
	 * instances from the {@link JwtAuthority}.
	 */
	public static final String INPUT = "retrieveKeys";

	/*
	 * ==================== SectionSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure the retrieve function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("FUNCTIONS",
				new CombinedServerRetrieveValidateKeysManagedFunctionSource());
		SectionFunction retrieveJwtValidateKeys = namespace.addSectionFunction(INPUT, INPUT);

		// Link input
		designer.link(designer.addSectionInput(INPUT, JwtValidateKeyCollector.class.getName()),
				retrieveJwtValidateKeys);

		// Link dependencies
		retrieveJwtValidateKeys.getFunctionObject(Dependencies.JWT_VALIDATE_KEY_COLLECTOR.name()).flagAsParameter();
		designer.link(retrieveJwtValidateKeys.getFunctionObject(Dependencies.JWT_AUTHORITY.name()),
				designer.addSectionObject(Dependencies.JWT_AUTHORITY.name(), JwtAuthority.class.getName()));
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		JWT_VALIDATE_KEY_COLLECTOR, JWT_AUTHORITY
	}

	/**
	 * JWKS publish {@link ManagedFunctionSource}.
	 */
	@PrivateSource
	private static class CombinedServerRetrieveValidateKeysManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the function
			ManagedFunctionTypeBuilder<Dependencies, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT, () -> (functionContext) -> {

						// Obtain the dependencies
						JwtValidateKeyCollector collector = (JwtValidateKeyCollector) functionContext
								.getObject(Dependencies.JWT_VALIDATE_KEY_COLLECTOR);
						JwtAuthority<?> authority = (JwtAuthority<?>) functionContext
								.getObject(Dependencies.JWT_AUTHORITY);

						// Load the validate keys
						collector.setKeys(authority.getActiveJwtValidateKeys());

						// Nothing further
						return null;
					}, Dependencies.class, None.class);
			function.addObject(JwtValidateKeyCollector.class).setKey(Dependencies.JWT_VALIDATE_KEY_COLLECTOR);
			function.addObject(JwtAuthority.class).setKey(Dependencies.JWT_AUTHORITY);
		}
	}

}