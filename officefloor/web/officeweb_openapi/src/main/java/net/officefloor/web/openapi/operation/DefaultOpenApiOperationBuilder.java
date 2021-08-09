/*-
 * #%L
 * OpenAPI
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

package net.officefloor.web.openapi.operation;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.openapi.response.ObjectResponseAnnotation;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.value.load.ValueLoaderFactory;
import net.officefloor.web.value.load.ValueLoaderSource;
import net.officefloor.web.value.load.ValueName;

/**
 * Default {@link OpenApiOperationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultOpenApiOperationBuilder implements OpenApiOperationBuilder {

	/**
	 * Listing of unhandled {@link Escalation} types.
	 */
	private final List<Class<?>> unhandledEscalations = new LinkedList<>();

	/**
	 * Obtains the listing of unhandled {@link Escalation} types.
	 * 
	 * @return Listing of unhandled {@link Escalation} types.
	 */
	public Class<?>[] getUnhandledEsclationTypes() {
		return this.unhandledEscalations.toArray(new Class[this.unhandledEscalations.size()]);
	}

	/**
	 * Instantiate.
	 * 
	 * @param context {@link OpenApiOperationContext}.
	 */
	public DefaultOpenApiOperationBuilder(OpenApiOperationContext context) {

		// Provide possible description
		String description = context.getHttpInput().getDocumentation();
		if ((description != null) && (description.trim().length() > 0)) {

			// Determine summary
			int sentenceDelimit = description.indexOf('.');
			int newLineDelimit = description.indexOf('\n');
			Function<Integer, Integer> handleNotFound = (position) -> position < 0 ? Integer.MAX_VALUE : position;
			int delimit = Math.min(handleNotFound.apply(sentenceDelimit), handleNotFound.apply(newLineDelimit));
			int summaryLength = Math.min(description.length(), delimit);
			String summary = description.substring(0, summaryLength);

			// Provide description
			Operation operation = context.getOperation();
			operation.setSummary(summary);
			operation.setDescription(description);
		}
	}

	/*
	 * ================== OpenApiOperationBuilder ========================
	 */

	@Override
	public void buildInManagedFunction(OpenApiOperationFunctionContext context) throws Exception {

		// Obtain the type
		ExecutionManagedFunction managedFunction = context.getManagedFunction();
		ManagedFunctionType<?, ?> type = managedFunction.getManagedFunctionType();

		// Obtain the operation
		Operation operation = context.getOperation();

		// Register possible security
		HttpAccess httpAccess = type.getAnnotation(HttpAccess.class);
		if (httpAccess != null) {
			String httpSecurityName = httpAccess.withHttpSecurity();
			this.addSecurityRequirement(httpSecurityName, context);
		}

		// Determine if tag
		Tag tag = type.getAnnotation(Tag.class);
		if (tag != null) {
			this.addTag(tag, context);
		}

		// Determine if multiple tags
		Tags tags = type.getAnnotation(Tags.class);
		if (tags != null) {
			for (Tag multipleTag : tags.value()) {
				this.addTag(multipleTag, context);
			}
		}

		// Load managed function specification
		for (ManagedFunctionObjectType<?> objectType : type.getObjectTypes()) {

			// Include possible security
			if (HttpAccessControl.class.equals(objectType.getObjectType())) {
				String httpSecurityName = objectType.getTypeQualifier();
				this.addSecurityRequirement(httpSecurityName, context);
			}

			// Include possible path parameter
			HttpPathParameter pathParam = objectType.getAnnotation(HttpPathParameter.class);
			if (pathParam != null) {
				String paramName = pathParam.value();
				this.addParameter(paramName, () -> new PathParameter().name(paramName), objectType, operation, context);
			}

			// Include possible query parameter
			HttpQueryParameter queryParam = objectType.getAnnotation(HttpQueryParameter.class);
			if (queryParam != null) {
				String paramName = queryParam.value();
				this.addParameter(paramName, () -> new QueryParameter().name(paramName), objectType, operation,
						context);
			}

			// Include possible header parameter
			HttpHeaderParameter headerParam = objectType.getAnnotation(HttpHeaderParameter.class);
			if (headerParam != null) {
				String paramName = headerParam.value();
				this.addParameter(paramName, () -> new HeaderParameter().name(paramName), objectType, operation,
						context);
			}

			// Include possible cookie parameter
			HttpCookieParameter cookieParam = objectType.getAnnotation(HttpCookieParameter.class);
			if (cookieParam != null) {
				String paramName = cookieParam.value();
				this.addParameter(paramName, () -> new CookieParameter().name(paramName), objectType, operation,
						context);
			}

			// Include possible HTTP Parameters
			if (objectType.getAnnotation(HttpParameters.class) != null) {

				// Obtain the HTTP parameter names
				Class<?> httpParametersType = objectType.getObjectType();
				ValueLoaderSource loaderSource = new ValueLoaderSource(httpParametersType, false, new HashMap<>(),
						null);
				ValueLoaderFactory<?> loaderFactory = loaderSource.sourceValueLoaderFactory(httpParametersType);
				ValueName[] names = loaderFactory.getValueNames();

				// Load names in sorted order
				Arrays.sort(names, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
				for (ValueName name : names) {
					String paramName = name.getName();
					HttpValueLocation location = name.getLocation();
					if (location == null) {
						// Attempt to default location
						String routePath = context.getHttpInput().getRoutePath();
						location = (routePath.toLowerCase().contains("{" + paramName.toLowerCase() + "}"))
								? HttpValueLocation.PATH
								: HttpValueLocation.QUERY;
					}
					switch (location) {
					case PATH:
						this.addParameter(paramName, () -> new PathParameter().name(paramName), objectType, operation,
								context);
						break;
					case ENTITY:
					case QUERY:
						this.addParameter(paramName, () -> new QueryParameter().name(paramName), objectType, operation,
								context);
						break;
					case HEADER:
						this.addParameter(paramName, () -> new HeaderParameter().name(paramName), objectType, operation,
								context);
						break;
					case COOKIE:
						this.addParameter(paramName, () -> new CookieParameter().name(paramName), objectType, operation,
								context);
						break;
					default:
						throw new IllegalStateException(
								"Unknown  " + HttpValueLocation.class.getSimpleName() + " " + location);
					}
				}
			}

			// Include possible HTTP Object
			if (objectType.getAnnotation(HttpObject.class) != null) {

				// Obtain the request body schema
				Class<?> httpObjectType = objectType.getObjectType();
				ResolvedSchema resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(httpObjectType);

				// Add the request body
				Content content = new Content();

				// Load the handled content types
				boolean isIncludeHttpObject = false;
				for (HttpObjectParserFactory objectParserFactory : context.getHttpInput()
						.getHttpObjectParserFactories()) {

					// Determine if can handle object type
					boolean isHandleHttpObject;
					try {
						isHandleHttpObject = objectParserFactory.createHttpObjectParser(httpObjectType) != null;
					} catch (Exception ex) {
						isHandleHttpObject = false;
					}

					// Able to handle input of type
					if (isHandleHttpObject) {
						content.addMediaType(objectParserFactory.getContentType(),
								new MediaType().schema(resolvedSchema.schema));
						operation.requestBody(new RequestBody().content(content));
						isIncludeHttpObject = true;
					}
				}

				// Ensure referenced schemas are included
				if (isIncludeHttpObject) {
					resolvedSchema.referencedSchemas
							.forEach((key, schema) -> context.getComponents().addSchemas(key, schema));
				}
			}

			// Include possible Object Responder
			Class<?> objectClass = objectType.getObjectType();
			if (ObjectResponse.class.equals(objectClass)) {

				// Obtain the response information
				int statusCode;
				Type responseType;
				ObjectResponseAnnotation responseAnnotation = type.getAnnotation(ObjectResponseAnnotation.class);
				if (responseAnnotation != null) {
					// Use specific information of response
					statusCode = responseAnnotation.getStatusCode();
					responseType = responseAnnotation.getResponseType();
				} else {
					// Use defaults
					statusCode = HttpStatus.OK.getStatusCode();
					responseType = Object.class;
				}

				// Determine if response details
				Class<?> responseClass;
				boolean isArray;
				Type schemaType;
				if (responseType instanceof Class) {
					// Class so determine if array
					responseClass = (Class<?>) responseType;
					isArray = responseClass.isArray();
					schemaType = isArray ? responseClass.getComponentType() : responseType;
				} else {
					// Not class, so determine
					responseClass = Object.class;
					isArray = false;
					schemaType = responseType;
				}

				// Obtain the response schema
				ResolvedSchema resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(schemaType);

				// Determine schema (handle if array)
				Schema<?> responseSchema;
				if (isArray) {
					responseSchema = new ArraySchema().type("array").items(resolvedSchema.schema);
				} else {
					responseSchema = resolvedSchema != null ? resolvedSchema.schema : null;
				}

				// Lazy add responses
				ApiResponses responses = operation.getResponses();
				if (responses == null) {
					responses = new ApiResponses();
					operation.setResponses(responses);
				}

				// Load the response content types
				boolean isIncludeResponse = false;
				for (HttpObjectResponderFactory objectResponderFactory : context.getHttpInput()
						.getHttpObjectResponderFactories()) {

					// Determine if can send object type
					boolean isHandleResponse;
					try {
						isHandleResponse = objectResponderFactory.createHttpObjectResponder(responseClass) != null;
					} catch (Exception ex) {
						isHandleResponse = false;
					}

					// Able to send response
					if (isHandleResponse) {
						Content content = new Content();
						content.put(objectResponderFactory.getContentType(), new MediaType().schema(responseSchema));
						responses.addApiResponse(String.valueOf(statusCode), new ApiResponse().content(content));
						isIncludeResponse = true;
					}
				}

				// Ensure referenced schemas are included
				if (isIncludeResponse) {
					resolvedSchema.referencedSchemas
							.forEach((key, schema) -> context.getComponents().addSchemas(key, schema));
				}
			}
		}

		// Determine if escalated exception (no handler managed function)
		for (ManagedFunctionEscalationType escalationType : type.getEscalationTypes()) {
			if (managedFunction.getManagedFunction(escalationType) == null) {

				// Add unhandled escalation
				this.unhandledEscalations.add(escalationType.getEscalationType());
			}
		}
	}

	@Override
	public void buildComplete(OpenApiOperationContext context) throws Exception {

		// Ensure have responses
		Operation operation = context.getOperation();
		ApiResponses responses = operation.getResponses();
		if (responses == null) {
			responses = new ApiResponses();
			operation.setResponses(responses);
		}

		// Ensure have response
		if (responses.size() == 0) {

			// Provide response
			ApiResponse response = new ApiResponse();
			responses.addApiResponse("200", response);
			response.content(new Content().addMediaType("application/octet-stream", new MediaType()));
		}
	}

	/**
	 * Adds {@link SecurityRequirement}.
	 * 
	 * @param httpSecurityName Name of {@link HttpSecurity}.
	 * @param context          {@link OpenApiOperationFunctionContext}.
	 */
	private void addSecurityRequirement(String httpSecurityName, OpenApiOperationFunctionContext context) {
		if (!CompileUtil.isBlank(httpSecurityName)) {
			// Must use specific security
			context.getOrAddSecurityRequirement(httpSecurityName);
		} else {
			// Any security provides access
			for (String securityName : context.getAllSecurityNames()) {
				context.getOrAddSecurityRequirement(securityName);
			}
		}
	}

	/**
	 * Adds the {@link Tag}.
	 * 
	 * @param tag     {@link Tag}.
	 * @param context {@link OpenApiOperationFunctionContext}.
	 */
	private void addTag(Tag tag, OpenApiOperationFunctionContext context) {

		// Add the tag name (only once)
		String tagName = tag.name();
		Operation operation = context.getOperation();
		if ((operation.getTags() == null) || (!operation.getTags().contains(tagName))) {
			operation.addTagsItem(tagName);
		}

		// Include details if description
		String tagDescription = tag.description();
		if (!CompileUtil.isBlank(tagDescription)) {
			context.getOpenApi()
					.addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name(tagName).description(tagDescription));
		}
	}

	/**
	 * Add {@link Parameter}.
	 * 
	 * @param name       Name.
	 * @param factory    Factory to create {@link Parameter}.
	 * @param objectType {@link ManagedFunctionObjectType} for the
	 *                   {@link Parameter}.
	 * @param operation  {@link Operation}.
	 * @param context    {@link OpenApiOperationFunctionContext}.
	 */
	private void addParameter(String name, Supplier<Parameter> factory, ManagedFunctionObjectType<?> objectType,
			Operation operation, OpenApiOperationFunctionContext context) {
		Parameter parameter = context.getParameter(name);
		if (parameter == null) {

			// Create the parameter
			parameter = factory.get();

			// Always String
			parameter.schema(new Schema<String>().type("string"));

			// Add in OpenAPI annotated information
			io.swagger.v3.oas.annotations.Parameter documentation = objectType
					.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
			if (documentation != null) {
				parameter.setDescription(documentation.description());
				if (documentation.required()) {
					parameter.setRequired(true);
				}
				parameter.setExample(documentation.example());
			}

			// Include the parameter
			operation.addParametersItem(parameter);
		}
	}

}
