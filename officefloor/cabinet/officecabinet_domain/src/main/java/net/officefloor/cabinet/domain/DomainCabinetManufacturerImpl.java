package net.officefloor.cabinet.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.Index.IndexField;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;

/**
 * {@link DomainCabinetManufacturer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainCabinetManufacturerImpl implements DomainCabinetManufacturer {

	/**
	 * Extracts the return type as first parameter type.
	 * 
	 * @param returnRawType Expected raw return type.
	 * @param method        {@link Method}.
	 * @return First parameter type or <code>null</code>.
	 */
	private static Class<?> extractReturnType(Class<?> returnRawType, Method method) {
		if (returnRawType.isAssignableFrom(method.getReturnType())) {
			return extractFirstParameterType(method.getGenericReturnType());
		}
		return null;
	}

	/**
	 * <p>
	 * Obtains the first parameter type of input {@link Type}.
	 * <p>
	 * Typically this is {@link Optional} contained type or {@link Iterator} element
	 * type.
	 * 
	 * @param type {@link Type}.
	 * @return First parameter type.
	 */
	private static Class<?> extractFirstParameterType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			Type[] parameterTypes = paramType.getActualTypeArguments();
			if (parameterTypes.length > 0) {
				Type parameterType = parameterTypes[0];
				if (parameterType instanceof Class) {
					return (Class<?>) parameterType;
				}
			}
		}
		return null;
	}

	/**
	 * {@link ClassLoader} to load domain {@link OfficeCabinet} instances.
	 */
	private final ClassLoader classLoader;

	/**
	 * Interrogators to translate {@link Method} return type to {@link Document}
	 * type.
	 */
	private final List<Function<Method, Class<?>>> returnTypeInterrogators = new ArrayList<>();

	/**
	 * Instantiate.
	 */
	public DomainCabinetManufacturerImpl(ClassLoader classLoader) {
		this.classLoader = classLoader;

		// Load the default return type interrogators
		this.returnTypeInterrogators.add((method) -> {
			Class<?> returnType = method.getReturnType();
			if ((returnType != null) && (returnType.isAnnotationPresent(Document.class))) {
				return returnType; // nullable document return
			} else {
				return null; // not a document
			}
		});
		this.returnTypeInterrogators.add((method) -> extractReturnType(Optional.class, method));
		this.returnTypeInterrogators.add((method) -> extractReturnType(Iterator.class, method));
	}

	/*
	 * ==================== DomainCabinetManufacturer =====================
	 */

	@Override
	public <C> DomainCabinetFactory<C> createDomainCabinetFactory(Class<C> cabinetType) throws Exception {

		// Indexes required for the document type
		Map<Class<?>, Set<Index>> documentTypeIndexes = new HashMap<>();

		// Method implementations
		Map<String, MethodImplementation> methodImplementations = new HashMap<>();

		// Cache fields of document improve performance
		Map<Class<?>, List<DocumentField>> cachedDocumentFields = new HashMap<>();

		// Create servicing for each method
		for (Method method : cabinetType.getMethods()) {

			// Obtain parameters (as require field per parameter)
			Class<?>[] paramTypes = method.getParameterTypes();

			// Determine if return document
			Class<?> documentType = null;
			FOUND_RETURN_TYPE: for (Function<Method, Class<?>> returnTypeInterrogator : this.returnTypeInterrogators) {
				documentType = returnTypeInterrogator.apply(method);
				if (documentType != null) {
					break FOUND_RETURN_TYPE;
				}
			}
			if (documentType != null) {

				// Retrieving the document fields
				List<DocumentField> documentFields = cachedDocumentFields.get(documentType);
				if (documentFields == null) {
					List<DocumentField> fields = new ArrayList<>();
					CabinetUtil.processFields(documentType,
							(context) -> fields.add(new DocumentField(context.getField(), context.isKey())));
					cachedDocumentFields.put(documentType, fields);
					documentFields = fields;
				}

				// Work backwards on method name to determine the query fields
				LinkedList<DocumentField> queryFields = new LinkedList<>();
				String remainingQuery = method.getName();
				do {

					// Find potentially matching fields
					List<DocumentField> potentialMatches = new ArrayList<>(2);
					for (DocumentField documentField : documentFields) {
						if (remainingQuery.endsWith(documentField.propertyName)) {
							potentialMatches.add(documentField);
						}
					}
					switch (potentialMatches.size()) {
					case 0:
						throw new InvalidCabinetException(cabinetType.getName() + "#" + remainingQuery + " ("
								+ method.getName() + ") is not aligned to any field of " + documentType.getName());

					case 1:
						// Include field for query
						DocumentField queryField = potentialMatches.get(0);
						queryFields.push(queryField);

						// Removing field from query
						remainingQuery = remainingQuery.substring(0,
								remainingQuery.length() - queryField.propertyName.length());
						break;

					default:
						List<String> matchedFields = potentialMatches.stream().map((match) -> match.fieldName)
								.collect(Collectors.toList());
						throw new InvalidCabinetException(cabinetType.getName() + "#" + remainingQuery + " ("
								+ method.getName() + ") matches multiple fields " + String.join(",", matchedFields));
					}

				} while (queryFields.size() < paramTypes.length);

				// Ensure the parameters types align
				if (paramTypes.length != queryFields.size()) {
					List<String> parameterFieldNames = queryFields.stream().map((queryField) -> queryField.fieldName)
							.collect(Collectors.toList());
					throw new InvalidCabinetException(cabinetType.getName() + "#" + remainingQuery + " ("
							+ method.getName() + ") incorrect number of parameters.  Expecting parameters for "
							+ String.join(",", parameterFieldNames));
				}
				for (int i = 0; i < paramTypes.length; i++) {
					Class<?> paramType = paramTypes[i];
					DocumentField paramField = queryFields.get(i);
					if (!paramType.equals(paramField.fieldType)) {
						throw new InvalidCabinetException(cabinetType.getName() + "#" + remainingQuery + " ("
								+ method.getName() + ") incorrect type for parameter " + i + " (" + paramField.fieldName
								+ ") as expecting " + paramField.fieldType.getName() + " but was "
								+ paramType.getName());
					}
				}

				// Add the index for the document type
				Set<Index> indexes = documentTypeIndexes.get(documentType);
				if (indexes == null) {
					indexes = new HashSet<>();
					documentTypeIndexes.put(documentType, indexes);
				}
				if (queryFields.stream().allMatch((queryField) -> queryField.isKey)) {

					// Determine return type
					MethodImplementation retrieveByKeyMethod;
					if (method.getReturnType().isAnnotationPresent(Document.class)) {
						// Returning the document
						retrieveByKeyMethod = new RetrieveNullableByKeyMethodImplementation<>(documentType);
					} else {
						// Returning optional to the document
						retrieveByKeyMethod = new RetrieveByKeyMethodImplementation<>(documentType);
					}

					// Provide retrieve by key method implementation
					methodImplementations.put(method.getName(), retrieveByKeyMethod);

				} else {
					// Not look up by key
					IndexField[] indexFields = queryFields.stream()
							.map((queryField) -> new IndexField(queryField.fieldName)).toArray(IndexField[]::new);
					indexes.add(new Index(indexFields));

					// Provide retrieve by query method implementation
					String[] queryFieldNames = queryFields.stream().map((queryField) -> queryField.fieldName)
							.toArray(String[]::new);
					methodImplementations.put(method.getName(),
							new RetrieveByQueryMethodImplementation<>(documentType, queryFieldNames));
				}

			} else {
				// Running save
				SaveParameter[] saveParameters = new SaveParameter[paramTypes.length];
				for (int index = 0; index < paramTypes.length; index++) {
					Class<?> paramType = paramTypes[index];

					// Add saving document to meta-data
					Set<Index> indexes = documentTypeIndexes.get(paramType);
					if (indexes == null) {
						// Include document in meta-data
						documentTypeIndexes.put(paramType, new HashSet<>());
					}

					// TODO handle collection and array
					saveParameters[index] = new DocumentSaveParameter<>(paramType);
				}

				// Provide save method implementation
				methodImplementations.put(method.getName(), new SaveMethodImplementation(saveParameters));
			}
		}

		// Obtain the document meta-data
		DomainCabinetDocumentMetaData[] metaData = new DomainCabinetDocumentMetaData[documentTypeIndexes.size()];
		int index = 0;
		for (Class<?> documentType : documentTypeIndexes.keySet()) {
			Set<Index> indexes = documentTypeIndexes.get(documentType);
			metaData[index++] = new DomainCabinetDocumentMetaData(documentType, indexes.toArray(Index[]::new));
		}

		// Create and return factory
		return new DomainCabinetFactoryImpl<>(cabinetType, methodImplementations, metaData, this.classLoader);
	}

	private static class DocumentField {
		private final String fieldName;
		private final String propertyName;
		private final Class<?> fieldType;
		private final boolean isKey;

		private DocumentField(Field field, boolean isKey) {
			this.fieldName = field.getName();
			this.propertyName = this.fieldName.substring(0, 1).toUpperCase() + this.fieldName.substring(1);
			this.fieldType = field.getType();
			this.isKey = isKey;
		}
	}

}