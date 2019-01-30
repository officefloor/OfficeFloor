package net.officefloor.web.jwt.authority;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
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
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.web.jwt.authority.JwtAuthorityManagedObjectSource.Flows;
import net.officefloor.web.jwt.spi.encode.JwtEncodeCollector;
import net.officefloor.web.jwt.spi.repository.JwtAuthorityRepository;

/**
 * {@link JwtAuthority} {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthoritySectionSource extends AbstractSectionSource {

	/**
	 * {@link SectionInput} name to handle {@link Flows#RETRIEVE_ENCODE_KEYS}.
	 */
	public static final String INPUT_RETRIEVE_ENCODE_KEYS = Flows.RETRIEVE_ENCODE_KEYS.name();

	/*
	 * ==================== SectionSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Add the repository dependency
		SectionObject jwtAuthorityRepository = designer.addSectionObject(JwtAuthorityRepository.class.getName(),
				JwtAuthorityRepository.class.getName());

		// Add the handling functions
		SectionFunctionNamespace functions = designer.addSectionFunctionNamespace("functions",
				new JwtAuthorityManagedFunctionSource());

		// Configure handling retrieving encode keys
		SectionInput retrieveEncodeKeysInput = designer.addSectionInput(INPUT_RETRIEVE_ENCODE_KEYS,
				JwtEncodeCollector.class.getName());
		SectionFunction retrieveEncodeKeysFunction = functions.addSectionFunction(INPUT_RETRIEVE_ENCODE_KEYS,
				INPUT_RETRIEVE_ENCODE_KEYS);
		designer.link(retrieveEncodeKeysInput, retrieveEncodeKeysFunction);
		retrieveEncodeKeysFunction.getFunctionObject(Dependencies.COLLECTOR.name()).flagAsParameter();
		designer.link(retrieveEncodeKeysFunction.getFunctionObject(Dependencies.JWT_AUTHORITY_REPOSITORY.name()),
				jwtAuthorityRepository);
	}

	/**
	 * Dependency keys for the {@link ManagedFunction} instances.
	 */
	private static enum Dependencies {
		COLLECTOR, JWT_AUTHORITY_REPOSITORY
	}

	/**
	 * {@link JwtAuthority} {@link ManagedFunctionSource}.
	 */
	private static class JwtAuthorityManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ================= ManagedFunctionSource =============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the clock
			Clock<Long> clock = context.getClock((time) -> time);

			// Add the retrieve encode keys handler
			ManagedFunctionTypeBuilder<Dependencies, None> retrieveEncodeKeysFunction = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT_RETRIEVE_ENCODE_KEYS, () -> (functionContext) -> {

						// Obtain the JWT authority repository
						JwtEncodeCollector collector = (JwtEncodeCollector) functionContext
								.getObject(Dependencies.COLLECTOR);
						JwtAuthorityRepository repository = (JwtAuthorityRepository) functionContext
								.getObject(Dependencies.JWT_AUTHORITY_REPOSITORY);

						// Load the JWT encode keys
						JwtAuthorityManagedObjectSource.loadJwtEncodeKeys(clock, repository, collector);

						// Nothing further
						return null;
					}, Dependencies.class, None.class);
			retrieveEncodeKeysFunction.addObject(JwtEncodeCollector.class).setKey(Dependencies.COLLECTOR);
			retrieveEncodeKeysFunction.addObject(JwtAuthorityRepository.class)
					.setKey(Dependencies.JWT_AUTHORITY_REPOSITORY);
		}
	}

}