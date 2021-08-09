/*-
 * #%L
 * JWT Authority
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

package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.jwt.authority.JwtAuthority;
import net.officefloor.web.jwt.jwks.JwksSectionSource;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * JWKS publish {@link SectionSource}.
 * <p>
 * Publishes the {@link JwtValidateKey} instances from the {@link JwtAuthority}
 * for a {@link JwksSectionSource} (or other JWKS consumer) to consume.
 * 
 * @author Daniel Sagenschneider
 */
public class JwksPublishSectionSource extends AbstractSectionSource {

	/**
	 * Loads the {@link JwksKeyWriter} instances.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link JwksKeyWriter} instances.
	 */
	public static JwksKeyWriter<?>[] loadJwksKeyWriters(SourceContext context) {

		// Load the JWKS key writers
		List<JwksKeyWriter<?>> keyWritersList = new ArrayList<>();
		for (JwksKeyWriter<?> keyWriter : context.loadServices(JwksKeyWriterServiceFactory.class, null)) {
			keyWritersList.add(keyWriter);
		}
		JwksKeyWriter<?>[] keyWriters = keyWritersList.toArray(new JwksKeyWriter[keyWritersList.size()]);

		// Return the key writers
		return keyWriters;
	}

	/**
	 * Writes the {@link Key}.
	 * 
	 * @param key        {@link Key}.
	 * @param keyWriters {@link JwksKeyWriter} instances.
	 * @return Written {@link Key} or <code>null</code> if unable to write the
	 *         {@link Key}.
	 * @throws Exception If fails to write the {@link Key}.
	 */
	public static String writeKey(Key key, JwksKeyWriter<?>[] keyWriters) throws Exception {

		// Write out the key node
		JwksKeyWriterContext<?> keyContext = writeKeyNode(key, keyWriters);
		if (keyContext == null) {
			return null; // unable to write key
		}

		// Return the key
		return mapper.writeValueAsString(keyContext.getKeyNode());
	}

	/**
	 * Writes the {@link Key} to a {@link JsonNode}.
	 * 
	 * @param key        {@link Key}.
	 * @param keyWriters {@link JwksKeyWriter} instances.
	 * @return {@link JsonNode} for the {@link Key} or <code>null</code> if unable
	 *         to write the {@link Key}.
	 * @throws Exception If fails to write the {@link Key}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static JwksKeyWriterContext<?> writeKeyNode(Key key, JwksKeyWriter<?>[] keyWriters) throws Exception {

		// Write the JWKS key
		NEXT_KEY_WRITER: for (JwksKeyWriter<?> keyWriter : keyWriters) {

			// Determine if can be written
			if (!keyWriter.canWriteKey(key)) {
				continue NEXT_KEY_WRITER;
			}

			// Able to write key
			ObjectNode keyNode = mapper.getNodeFactory().objectNode();
			JwksKeyWriterContext writerContext = new JwksKeyWriterContext() {

				@Override
				public Object getKey() {
					return key;
				}

				@Override
				public ObjectNode getKeyNode() {
					return keyNode;
				}

				@Override
				public JsonNodeFactory getNodeFactory() {
					return mapper.getNodeFactory();
				}
			};
			keyWriter.writeKey(writerContext);

			// Return the context (for access to key node)
			return writerContext;
		}

		// As here, unable to write the key
		return null;
	}

	/**
	 * Name of {@link SectionInput} to publish the {@link JwtValidateKey} instances.
	 */
	public static final String INPUT = "publish";

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/*
	 * ================= SectionSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure the function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(INPUT,
				new JwksPublishManagedFunctionSource());
		SectionFunction function = namespace.addSectionFunction(INPUT, INPUT);

		// Link the input
		designer.link(designer.addSectionInput(INPUT, null), function);

		// Link the dependencies
		designer.link(function.getFunctionObject(Dependencies.JWT_AUTHORITY.name()),
				designer.addSectionObject(Dependencies.JWT_AUTHORITY.name(), JwtAuthority.class.getName()));
		designer.link(function.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), designer
				.addSectionObject(Dependencies.SERVER_HTTP_CONNECTION.name(), ServerHttpConnection.class.getName()));
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		JWT_AUTHORITY, SERVER_HTTP_CONNECTION
	}

	/**
	 * JWKS publish {@link ManagedFunctionSource}.
	 */
	@PrivateSource
	private static class JwksPublishManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Load the JWKS key writers
			JwksKeyWriter<?>[] keyWriters = JwksPublishSectionSource.loadJwksKeyWriters(context);

			// Add the function
			ManagedFunctionTypeBuilder<Dependencies, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT, Dependencies.class, None.class)
					.setFunctionFactory(() -> (functionContext) -> {

						// Obtain the dependencies
						JwtAuthority<?> authority = (JwtAuthority<?>) functionContext
								.getObject(Dependencies.JWT_AUTHORITY);
						ServerHttpConnection connection = (ServerHttpConnection) functionContext
								.getObject(Dependencies.SERVER_HTTP_CONNECTION);

						// Obtain the active validate keys
						JwtValidateKey[] validateKeys = authority.getActiveJwtValidateKeys();

						// Create the response JSON
						JsonNodeFactory nodeFactory = mapper.getNodeFactory();
						ObjectNode jwksNode = nodeFactory.objectNode();
						ArrayNode keysNode = nodeFactory.arrayNode();
						jwksNode.set("keys", keysNode);

						// Load the JWKS keys
						for (JwtValidateKey validateKey : validateKeys) {

							// Write the JWKS key
							Key key = validateKey.getKey();
							JwksKeyWriterContext<?> keyContext = writeKeyNode(key, keyWriters);

							// Ensure the key is written
							if (keyContext == null) {
								throw new HttpException(new Exception("No " + JwksKeyWriter.class.getSimpleName()
										+ " for key " + key.getAlgorithm()));
							}

							// Provide time window for key
							keyContext.setLong("nbf", validateKey.getStartTime());
							keyContext.setLong("exp", validateKey.getExpireTime());

							// Include the key
							keysNode.add(keyContext.getKeyNode());
						}

						// Write out the JWKS response
						mapper.writeValue(connection.getResponse().getEntity(), jwksNode);

					});
			function.addObject(JwtAuthority.class).setKey(Dependencies.JWT_AUTHORITY);
			function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}
