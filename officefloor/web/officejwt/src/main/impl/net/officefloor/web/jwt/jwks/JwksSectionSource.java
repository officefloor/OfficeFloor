/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import net.officefloor.frame.api.source.SourceContext;
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
	 * Loads the {@link JwksKeyParser} instances.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link JwksKeyParser} instances.
	 */
	public static JwksKeyParser[] loadJwksKeyParsers(SourceContext context) {

		// Retrieve the JWKS key parsers
		List<JwksKeyParser> parserList = new ArrayList<>();
		for (JwksKeyParser parser : context.loadServices(JwksKeyParserServiceFactory.class, null)) {
			parserList.add(parser);
		}
		JwksKeyParser[] parsers = parserList.toArray(new JwksKeyParser[parserList.size()]);

		// Return the key parsers
		return parsers;
	}

	/**
	 * Parses out the {@link Key}.
	 * 
	 * @param serialisedKey Serialised {@link Key} in JWKS format.
	 * @param parsers       {@link JwksKeyParser} instances.
	 * @return {@link Key} or <code>null</code> if unable to parse out the
	 *         {@link Key}.
	 */
	public static Key parseKey(String serialisedKey, JwksKeyParser[] parsers) {

		// Parse out the JSON
		JsonNode keyNode;
		try {
			keyNode = mapper.readTree(serialisedKey);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}

		// Return the parsed key
		return parseKey(parsers, () -> keyNode);
	}

	/**
	 * Parses out the {@link Key}.
	 * 
	 * @param parsers       {@link JwksKeyParser} instances.
	 * @param parserContext {@link JwksKeyParserContext}.
	 * @return {@link Key} or <code>null</code> if unable to parse out the
	 *         {@link Key}.
	 */
	private static Key parseKey(JwksKeyParser[] parsers, JwksKeyParserContext parserContext) {

		// Parse out the key
		PARSED_KEY: for (JwksKeyParser parser : parsers) {
			try {
				Key key = parser.parseKey(parserContext);
				if (key != null) {
					return key; // key parsed
				}
			} catch (Exception ex) {
				continue PARSED_KEY;
			}
		}

		// As here, no key able to be parsed out
		return null;
	}

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
			JwksKeyParser[] parsers = JwksSectionSource.loadJwksKeyParsers(context);

			// Provide function to collect JWT validate keys
			ManagedFunctionTypeBuilder<Dependencies, None> retrieveJwtValidateKeys = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT, Dependencies.class, None.class)
					.setFunctionFactory(() -> (functionContext) -> {

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
									Key key = parseKey(parsers, parseContext);

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
					});
			retrieveJwtValidateKeys.addObject(JwtValidateKeyCollector.class)
					.setKey(Dependencies.JWT_VALIDATE_KEY_COLLECTOR);
			retrieveJwtValidateKeys.addObject(JwksRetriever.class).setKey(Dependencies.JWKS_RETRIEVER);
		}
	}

}
