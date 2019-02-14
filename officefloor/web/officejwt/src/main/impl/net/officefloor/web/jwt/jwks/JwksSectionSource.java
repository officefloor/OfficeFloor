package net.officefloor.web.jwt.jwks;

import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.jwt.validate.JwtValidateKey;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;

/**
 * <p>
 * JWKS {@link SectionSource}.
 * <p>
 * Provides logic to retrieve {@link JwtValidateKey} instances from a JWKS
 * service (via {@link JwksRetriever}).
 * 
 * @author Daniel Sagenschneider
 */
public class JwksSectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link SectionInput} to collect the {@link JwtValidateKey} instances.
	 */
	public static final String INPUT = "retrieveJwtValidateKeys";

	/**
	 * {@link ObjectMapper}.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/*
	 * =================== SectionSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure the retrieve function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("FUNCTIONS",
				new JwksManagedFunctionSource());
		SectionFunction retrieveJwtValidateKeys = namespace.addSectionFunction(INPUT, INPUT);

		// Link input
		designer.link(designer.addSectionInput(INPUT, JwtValidateKeyCollector.class.getName()),
				retrieveJwtValidateKeys);

		// Link dependencies
		retrieveJwtValidateKeys.getFunctionObject(Dependencies.JWT_VALIDATE_KEY_COLLECTOR.name()).flagAsParameter();
		designer.link(retrieveJwtValidateKeys.getFunctionObject(Dependencies.JWKS_RETRIEVER.name()),
				designer.addSectionObject(Dependencies.JWKS_RETRIEVER.name(), JwksRetriever.class.getName()));
	}

	private static enum Dependencies {
		JWT_VALIDATE_KEY_COLLECTOR, JWKS_RETRIEVER
	}

	/**
	 * JWKS {@link ManagedFunctionSource}.
	 */
	@PrivateSource
	private static class JwksManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ================== ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Retrieve the JWKS key parsers
			List<JwksKeyParser> parserList = new ArrayList<>();
			for (JwksKeyParser parser : context.loadServices(JwksKeyParserServiceFactory.class, null)) {
				parserList.add(parser);
			}
			JwksKeyParser[] parsers = parserList.toArray(new JwksKeyParser[parserList.size()]);

			// Provide function to collect JWT validate keys
			ManagedFunctionTypeBuilder<Dependencies, None> retrieveJwtValidateKeys = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT, () -> (functionContext) -> {

						// Obtain the dependencies
						JwtValidateKeyCollector collector = (JwtValidateKeyCollector) functionContext
								.getObject(Dependencies.JWT_VALIDATE_KEY_COLLECTOR);
						JwksRetriever retriever = (JwksRetriever) functionContext
								.getObject(Dependencies.JWKS_RETRIEVER);

						try {

							// Retrieve the JWKS content
							InputStream content = retriever.retrieveJwks();
							JsonNode jwksNode = mapper.readTree(content);

							// Obtain the keys
							JsonNode keysNode = jwksNode.get("keys");
							if (keysNode != null) {

								// Capture validate keys
								List<JwtValidateKey> validateKeys = new ArrayList<>(keysNode.size());

								// Load the keys
								for (JsonNode keyNode : keysNode) {

									// Create the JWKS key parse context
									JwksKeyParserContext parseContext = () -> keyNode;

									// Obtain the time window
									long nbf = parseContext.getLong(keyNode, "nbf", 0L);
									long exp = parseContext.getLong(keyNode, "exp", Long.MAX_VALUE);

									// Parse out the key
									Key key = null;
									PARSED_KEY: for (JwksKeyParser parser : parsers) {
										try {
											key = parser.parseKey(parseContext);
											if (key != null) {
												break PARSED_KEY;
											}
										} catch (Exception ex) {
											continue PARSED_KEY;
										}
									}

									// Add in the validate key
									if (key != null) {
										validateKeys.add(new JwtValidateKey(nbf, exp, key));
									}
								}

								// Load the keys
								collector.setKeys(validateKeys.toArray(new JwtValidateKey[validateKeys.size()]));
							}

						} catch (Exception ex) {
							collector.setFailure(ex, -1, null);
						}

						// Nothing further
						return null;
					}, Dependencies.class, None.class);
			retrieveJwtValidateKeys.addObject(JwtValidateKeyCollector.class)
					.setKey(Dependencies.JWT_VALIDATE_KEY_COLLECTOR);
			retrieveJwtValidateKeys.addObject(JwksRetriever.class).setKey(Dependencies.JWKS_RETRIEVER);
		}
	}

}