package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

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
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Load the JWKS key writers
			List<JwksKeyWriter<?>> keyWritersList = new ArrayList<>();
			for (JwksKeyWriter<?> keyWriter : context.loadServices(JwksKeyWriterServiceFactory.class, null)) {
				keyWritersList.add(keyWriter);
			}
			JwksKeyWriter<?>[] keyWriters = keyWritersList.toArray(new JwksKeyWriter[keyWritersList.size()]);

			// Add the function
			ManagedFunctionTypeBuilder<Dependencies, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(INPUT, () -> (functionContext) -> {

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
							NEXT_KEY_WRITER: for (JwksKeyWriter<?> keyWriter : keyWriters) {

								// Determine if can be written
								if (!keyWriter.canWriteKey(key)) {
									continue NEXT_KEY_WRITER;
								}

								// Able to write key
								ObjectNode keyNode = nodeFactory.objectNode();
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
										return nodeFactory;
									}
								};
								keyWriter.writeKey(writerContext);

								// Provide time window for key
								writerContext.setLong("nbf", validateKey.getStartTime());
								writerContext.setLong("exp", validateKey.getExpireTime());

								// Include the key
								keysNode.add(keyNode);
							}
						}

						// Write out the JWKS response
						mapper.writeValue(connection.getResponse().getEntity(), jwksNode);

						// Nothing further
						return null;
					}, Dependencies.class, None.class);
			function.addObject(JwtAuthority.class).setKey(Dependencies.JWT_AUTHORITY);
			function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}