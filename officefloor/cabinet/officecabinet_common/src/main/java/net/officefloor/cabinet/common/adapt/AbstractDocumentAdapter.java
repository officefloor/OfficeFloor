package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OneToOne;
import net.officefloor.cabinet.ReferenceAccess;
import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.common.metadata.SectionMetaData;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying store implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentAdapter<R, S> {

	/**
	 * Transforms the field value for the {@link Map}.
	 */
	@FunctionalInterface
	public static interface FieldValueTransform<I, O> {
		O transform(I inputValue);
	}

	/**
	 * Non-validating {@link FieldValidator}.
	 * 
	 * @return {@link FieldValidator}.
	 */
	public static FieldValidator notValidateField() {
		return (documentType, field) -> new FieldValidationResult();
	}

	/**
	 * {@link FieldLoader} that sets the value for the {@link Field}.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldLoader}.
	 */
	public static <V> FieldLoader<V> loadFieldBySet() {
		return (document, field, fieldValue, cabinetManager) -> field.set(document, fieldValue);
	}

	/**
	 * Non-translating translator.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueTranslator}.
	 */
	public static <V> FieldValueTranslator<V, V> translator() {
		return (fieldName, value) -> value;
	}

	/**
	 * {@link FieldValueTranslator} with {@link FieldValueTransform}.
	 * 
	 * @param <V>       {@link Field} type.
	 * @param <P>       Persistent value type.
	 * @param transform {@link FieldValueTransform}.
	 * @return {@link FieldValueTranslator}.
	 */
	public static <V, P> FieldValueTranslator<V, P> translator(FieldValueTransform<V, P> transform) {
		return (fieldName, value) -> value != null ? transform.transform(value) : null;
	}

	/**
	 * {@link FieldValueSerialiser} for {@link Field} value.
	 * 
	 * @param <V>        {@link Field} type.
	 * @param serialiser {@link Function} to serialise just {@link Field} value.
	 * @return {@link FieldValueSerialiser}.
	 */
	public static <V> FieldValueSerialiser<V> serialiser(Function<V, String> serialiser) {
		return (fieldName, fieldValue) -> fieldValue == null ? null : serialiser.apply(fieldValue);
	}

	/**
	 * {@link FieldValueSerialiser} for {@link Field} value.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueSerialiser} using default {@link Object#toString()}
	 *         to serialise value.
	 */
	public static <V> FieldValueSerialiser<V> serialiser() {
		return serialiser(Object::toString);
	}

	/**
	 * Indicates should never be serialised.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueSerialiser}.
	 */
	public static <V> FieldValueSerialiser<V> notSerialiseable() {
		return (fieldName, fieldValue) -> {
			throw new UnsupportedOperationException(
					"Should not be serialising field " + fieldName + " of type " + Map.class.getName());
		};
	}

	/**
	 * {@link FieldValueDeserialiser} for {@link Field} value.
	 * 
	 * @param <V>          {@link Field} type.
	 * @param deserialiser {@link Function} to deserialise just {@link Field} value.
	 * @return {@link FieldValueDeserialiser}.
	 */
	public static <V> FieldValueDeserialiser<V> deserialiser(Function<String, V> deserialiser) {
		return (fieldName, serialisedValue) -> (serialisedValue == null || serialisedValue.length() == 0) ? null
				: deserialiser.apply(serialisedValue);
	}

	/**
	 * {@link FieldValueDeserialiser} for a {@link Character} {@link Field} value.
	 * 
	 * @return {@link FieldValueDeserialiser} for {@link Character} {@link Field}
	 *         value.
	 */
	public static FieldValueDeserialiser<Character> charDeserialiser() {
		return deserialiser((text) -> text.charAt(0));
	}

	/**
	 * Indicates should never be deserialised.
	 * 
	 * @param <V>  {@link Field} type.
	 * @param type {@link Field} type.
	 * @return {@link FieldValueDeserialiser}.
	 */
	public static <V> FieldValueDeserialiser<V> notDeserialiseable(Class<?> type) {
		return (fieldName, serialisedValue) -> {
			throw new UnsupportedOperationException(
					"Should not be deserialising field " + fieldName + " of type " + type.getName());
		};
	}

	/**
	 * Means to initialise to typed internal {@link Document}.
	 */
	public class Initialise {

		/**
		 * Only instantiated for initialise.
		 */
		private Initialise() {
		}

		/**
		 * Specifies the {@link InternalDocumentFactory}.
		 * 
		 * @param internalDocumentFactory {@link InternalDocumentFactory}.
		 */
		public void setInternalDocumentFactory(InternalDocumentFactory<S> internalDocumentFactory) {
			AbstractDocumentAdapter.this.internalDocumentFactory = internalDocumentFactory;
		}

		/**
		 * Specifies the {@link KeyGetter}.
		 * 
		 * @param keyGetter {@link KeyGetter}.
		 */
		public void setKeyGetter(KeyGetter<R> keyGetter) {
			AbstractDocumentAdapter.this.keyGetter = keyGetter;
		}

		/**
		 * Specifies the {@link KeySetter}.
		 * 
		 * @param keySetter {@link KeySetter}.
		 */
		public void setKeySetter(KeySetter<S> keySetter) {
			AbstractDocumentAdapter.this.keySetter = keySetter;
		}

		/**
		 * Adds a {@link FieldType} via setting value.
		 * 
		 * @param <V>          Type of {@link Field} value.
		 * @param fieldType    Type of {@link Field} value.
		 * @param getter       {@link FieldValueGetter}.
		 * @param translator   {@link FieldValueTranslator}.
		 * @param setter       {@link FieldValueSetter}.
		 * @param serialiser   {@link FieldValueSerialiser}.
		 * @param deserialiser {@link FieldValueDeserialiser}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <P, V> void addFieldType(Class<V> fieldType, ScalarFieldValueGetter<R, V> getter,
				FieldValueTranslator<V, P> translator, FieldValueSetter<S, P> setter,
				FieldValueSerialiser<V> serialiser, FieldValueDeserialiser<V> deserialiser) {
			this.addFieldType(fieldType, notValidateField(), getter, loadFieldBySet(), translator, setter, serialiser,
					deserialiser);
		}

		/**
		 * Adds a {@link FieldType}.
		 * 
		 * @param <V>          Type of {@link Field} value.
		 * @param fieldType    Type of {@link Field} value.
		 * @param validator    {@link FieldValidator}.
		 * @param getter       {@link FieldValueGetter}.
		 * @param loader       {@link FieldLoader}.
		 * @param translator   {@link FieldValueTranslator}.
		 * @param setter       {@link FieldValueSetter}.
		 * @param serialiser   {@link FieldValueSerialiser}.
		 * @param deserialiser {@link FieldValueDeserialiser}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <P, L, V> void addFieldType(Class<V> fieldType, FieldValidator validator,
				ScalarFieldValueGetter<R, L> getter, FieldLoader<L> loader, FieldValueTranslator<V, P> translator,
				FieldValueSetter<S, P> setter, FieldValueSerialiser<V> serialiser,
				FieldValueDeserialiser<V> deserialiser) {
			FieldValueGetter<R, L> adaptGetter = (doc, fieldName, state, cabinetManager) -> getter.getValue(doc,
					fieldName);
			if (Map.class.equals(fieldType)) {
				AbstractDocumentAdapter.this.mapFieldType = new FieldType(validator, adaptGetter, loader, translator,
						setter, serialiser, deserialiser);
			} else {
				AbstractDocumentAdapter.this.fieldTypes.put(fieldType,
						new FieldType<>(validator, adaptGetter, loader, translator, setter, serialiser, deserialiser));

				// Capture the string getter
				if (String.class.equals(fieldType)) {
					AbstractDocumentAdapter.this.stringFieldValueGetter = (ScalarFieldValueGetter<R, String>) getter;
				}
			}
		}

		/**
		 * Adds a {@link FieldType} via setting value.
		 * 
		 * @param <V>            Type of the {@link Field} value.
		 * @param fieldType      Primitive type of the {@link Field} value.
		 * @param boxedFieldType Auto-boxed type for {@link Field}.
		 * @param getter         {@link FieldValueGetter}.
		 * @param translator     {@link FieldValueTranslator}.
		 * @param setter         {@link FieldValueSetter}.
		 * @param serialiser     {@link FieldValueSerialiser}.
		 * @param deserialiser   {@link FieldValueDeserialiser}.
		 */
		public <P, V> void addFieldType(Class<V> fieldType, Class<V> boxedFieldType,
				ScalarFieldValueGetter<R, V> getter, FieldValueTranslator<V, P> translator,
				FieldValueSetter<S, P> setter, FieldValueSerialiser<V> serialiser,
				FieldValueDeserialiser<V> deserialiser) {
			this.addFieldType(fieldType, boxedFieldType, notValidateField(), getter, loadFieldBySet(), translator,
					setter, serialiser, deserialiser);
		}

		/**
		 * Adds a {@link FieldType}.
		 * 
		 * @param <V>            Type of the {@link Field} value.
		 * @param fieldType      Primitive type of the {@link Field} value.
		 * @param boxedFieldType Auto-boxed type for {@link Field}.
		 * @param validator      {@link FieldValidator}.
		 * @param getter         {@link FieldValueGetter}.
		 * @param loader         {@link FieldLoader}.
		 * @param translator     {@link FieldValueTranslator}.
		 * @param setter         {@link FieldValueSetter}.
		 * @param serialiser     {@link FieldValueSerialiser}.
		 * @param deserialiser   {@link FieldValueDeserialiser}.
		 */
		public <P, V> void addFieldType(Class<V> fieldType, Class<V> boxedFieldType, FieldValidator validator,
				ScalarFieldValueGetter<R, V> getter, FieldLoader<V> loader, FieldValueTranslator<V, P> translator,
				FieldValueSetter<S, P> setter, FieldValueSerialiser<V> serialiser,
				FieldValueDeserialiser<V> deserialiser) {
			this.addFieldType(fieldType, validator, getter, loader, translator, setter, serialiser, deserialiser);
			this.addFieldType(boxedFieldType, validator, getter, loader, translator, setter, serialiser, deserialiser);
		}
	}

	/**
	 * {@link AbstractOfficeStore}.
	 */
	private final AbstractOfficeStore officeStore;

	/**
	 * Indicates if the top level {@link Document}.
	 */
	private final boolean isDocument;

	/**
	 * {@link InternalDocumentFactory}.
	 */
	private InternalDocumentFactory<S> internalDocumentFactory;

	/**
	 * {@link KeyGetter}.
	 */
	private KeyGetter<R> keyGetter;

	/**
	 * {@link KeySetter}.
	 */
	private KeySetter<S> keySetter;

	/**
	 * Mapping of {@link Field} type to {@link FieldType}.
	 */
	private final Map<Class<?>, FieldType<?, R, ?, S, ?>> fieldTypes = new HashMap<>();

	/**
	 * {@link Map} {@link FieldType}.
	 */
	private FieldType<Map<String, Object>, R, Map<String, Object>, S, Map<String, Object>> mapFieldType;

	/**
	 * {@link ScalarFieldValueGetter} for obtaining a {@link String}.
	 */
	private ScalarFieldValueGetter<R, String> stringFieldValueGetter;

	/**
	 * Instantiate as {@link Document}.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public AbstractDocumentAdapter(AbstractOfficeStore officeStore) {
		this(true, officeStore);
	}

	/**
	 * Instantiate.
	 * 
	 * @param isDocument  Indicates if top level {@link Document}.
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	@SuppressWarnings("unchecked")
	AbstractDocumentAdapter(boolean isDocument, AbstractOfficeStore officeStore) {
		this.isDocument = isDocument;
		this.officeStore = officeStore;

		// Create initialise context
		Initialise init = new Initialise();

		// Load default initialise
		this.defaultInitialise(init);

		// Initialise
		try {
			this.initialise(init);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialise " + AbstractDocumentAdapter.class.getSimpleName()
					+ " implementation " + this.getClass().getName(), ex);
		}

		// Obtain the string setter for the key
		FieldType<?, R, String, S, String> keySetter = (FieldType<?, R, String, S, String>) this.fieldTypes
				.get(String.class);

		// Load referencing
		FieldValidator oneToOneValidator = (documentType, field) -> {
			return null;
		};
		FieldLoader<String> oneToOneLoader = (document, field, fieldValue, cabinetManager) -> {

			// Obtain the reference
			Object oneToOneObject = field.get(document);
			OneToOne<Object> oneToOne = (OneToOne<Object>) oneToOneObject;

			// Obtain the cabinet for referenced document type
			Class<?> referencedDocumentType = oneToOne.documentType();
			OfficeCabinet cabinet = cabinetManager.getOfficeCabinet(referencedDocumentType);

			// Register function to retrieve the value
			ReferenceAccess.setRetriever(oneToOne, () -> {

				// Retrieve the referenced document from data store
				Optional<Object> referencedDocument = cabinet.retrieveByKey(fieldValue);
				
				// Return the referenced document
				return referencedDocument.orElse(null);
			});

		};
		FieldValueTranslator<OneToOne, Reference> oneToOneTranslator = (fieldName, fieldValue) -> {

			// Ensure have reference (otherwise treat as null)
			if (fieldValue == null) {
				return null;
			}

			// Translate to the referenced value
			Object referencedDocument = ReferenceAccess.nonRetrievedGet(fieldValue);

			// Return reference to the document
			return new Reference(referencedDocument);
		};
		FieldValueSetter<S, Reference> oneToOneSetter = (internalDocument, fieldName, fieldValue, change) -> {

			// Obtain the referenced document
			Object referencedDocument = fieldValue.getDocument();
			if (referencedDocument == null) {
				return; // nothing to set, as no referenced document
			}

			// Register the referenced document into the change
			String key = change.registerDocument(referencedDocument);

			// Load the key for internal document storage
			keySetter.setter.setValue(internalDocument, fieldName, key, change);
		};
		FieldValueSerialiser<OneToOne> oneToOneSerialiser = (fieldName, fieldValue) -> {
			return null;
		};
		FieldValueDeserialiser<OneToOne> oneToOneDeserialiser = (fieldName, fieldValue) -> {
			return null;
		};
		init.addFieldType(OneToOne.class, oneToOneValidator, this.stringFieldValueGetter, oneToOneLoader,
				oneToOneTranslator, oneToOneSetter, oneToOneSerialiser, oneToOneDeserialiser);

		// Ensure internal document factory initialised
		if (isDocument) {
			this.assertInitialise(() -> this.internalDocumentFactory == null,
					"Must specify " + InternalDocumentFactory.class.getSimpleName());

			// Ensure keys initialised
			this.assertInitialise(() -> this.keyGetter == null, "Must specify " + KeyGetter.class.getSimpleName());
			this.assertInitialise(() -> this.keySetter == null, "Must specify " + KeySetter.class.getSimpleName());

		} else {
			// Provide default internal document factory
			if (this.internalDocumentFactory == null) {
				this.internalDocumentFactory = () -> (S) new HashMap<>();
			}
		}

		// Ensure primitives initialised
		this.assertFieldTypes(boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class,
				char.class, Character.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
				double.class, Double.class);

		// Ensure open types initialised
		this.assertFieldTypes(String.class);

		// Ensure hierarchy loaded
		this.assertInitialise(() -> this.mapFieldType == null, "Must initialise field type " + Map.class.getName());

		// Ensure referncing types loaded
		this.assertFieldTypes(OneToOne.class);
	}

	/**
	 * Allows providing default initialise.
	 * 
	 * @param init {@link Initialise}.
	 */
	protected void defaultInitialise(Initialise init) {
		// No default initialise for document
	}

	/**
	 * Initialises static meta-data.
	 * 
	 * @param init {@link Initialise}.
	 * @throws Exception If fails to load the {@link Initialise}.
	 */
	protected abstract void initialise(Initialise init) throws Exception;

	/**
	 * Asserts appropriate initialise.
	 * 
	 * @param assertion Assertion.
	 * @param message   Message.
	 */
	private void assertInitialise(Supplier<Boolean> assertion, String message) {
		if (assertion.get()) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Asserts appropriate {@link Field} types have been initialised.
	 * 
	 * @param adapter {@link AbstractDocumentAdapter}.
	 * @param types   {@link Field} types.
	 */
	private void assertFieldTypes(Class<?>... types) {
		for (Class<?> type : types) {
			assertInitialise(() -> !this.fieldTypes.containsKey(type), "Must initialise field type " + type.getName());

			// Ensure field configured
			FieldType<?, R, ?, S, ?> fieldType = this.fieldTypes.get(type);
			assertInitialise(() -> fieldType.getter == null, "Must initialise getter for field type " + type.getName());
			assertInitialise(() -> fieldType.translator == null,
					"Must initialise translator for field type " + type.getName());
			assertInitialise(() -> fieldType.setter == null, "Must initialise setter for field type " + type.getName());
			assertInitialise(() -> fieldType.serialiser == null,
					"Must initialise serialiser for field type " + type.getName());
			assertInitialise(() -> fieldType.deserialiser == null,
					"Must initialise deserialiser for field type " + type.getName());
		}
	}

	/**
	 * Indicates if the top level {@link Document}.
	 * 
	 * @return <code>true</code> if top level {@link Document}.
	 */
	public boolean isDocument() {
		return this.isDocument;
	}

	/**
	 * Creates the internal {@link Document}.
	 * 
	 * @return Internal {@link Document}.
	 */
	public S createInternalDocument() {
		return this.internalDocumentFactory.createInternalDocument();
	}

	/**
	 * Obtains the {@link Key} value.
	 * 
	 * @param internalDocument Retrieved internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @return {@link Key} value.
	 */
	public String getKey(R internalDocument, String keyName) {
		return this.keyGetter.getKey(internalDocument, keyName);
	}

	/**
	 * Specifies the {@link Key} value on {@link InternalDocument} for storing.
	 * 
	 * @param internalDocument Stored internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @param keyValue         {@link Key} value.
	 */
	public void setStoreKey(S internalDocument, String keyName, String keyValue) {
		this.keySetter.setKey(internalDocument, keyName, keyValue);
	}

	/**
	 * Obtains the {@link FieldType} for the {@link Field} type.
	 * 
	 * @param <V>       Type of {@link Field}.
	 * @param fieldType {@link Field} type.
	 * @return {@link FieldType}.
	 */
	@SuppressWarnings("unchecked")
	public <V> FieldType<?, R, ?, S, ?> getFieldType(Class<V> fieldType) {

		// Obtain the type
		FieldType<?, R, ?, S, ?> type = this.fieldTypes.get(fieldType);

		// Object type, so determine load
		if (type == null) {

			// Create the section meta-data
			SectionMetaData<V> sectionMetaData;
			try {

				// Create the section adapter
				AbstractSectionAdapter sectionAdapter = this.officeStore.createSectionAdapter();

				// Create the section meta-data
				sectionMetaData = new SectionMetaData<>(sectionAdapter, fieldType);
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to create document meta-data for " + fieldType.getName(), ex);
			}

			// Create type for non-handled
			FieldValueGetter<R, V> getter = (internalDocument, fieldName, state, cabinetManager) -> {

				// Get sub section retrieved
				Map<String, Object> section = this.mapFieldType.getter.getValue(internalDocument, fieldName, state,
						cabinetManager);

				// With retrieved, create managed document
				V managedDocument = section != null
						? sectionMetaData.createManagedDocument(section, state, cabinetManager)
						: null;

				// Return the managed document
				return managedDocument;

			};
			FieldValueTranslator<V, V> translator = (fieldName, value) -> value;
			FieldValueSetter<S, V> setter = (internalDocument, fieldName, value, referencedDocumentHandler) -> {

				// Create section
				Map<String, Object> section = value != null
						? sectionMetaData.createInternalDocument(value, referencedDocumentHandler).getInternalDocument()
						: null;

				// Assign to input internal document
				this.mapFieldType.setter.setValue(internalDocument, fieldName, section, referencedDocumentHandler);
			};
			type = new FieldType<V, R, V, S, V>(notValidateField(), getter, loadFieldBySet(), translator, setter,
					(fieldName, fieldValue) -> {
						throw new UnsupportedOperationException(
								"Can not serialise Object into next bundle token for field " + fieldName);
					}, (fieldName, serialisedValue) -> {
						throw new UnsupportedOperationException("Can not deserialise Object from next bundle token");
					});
		}

		// Return the field type
		return (FieldType<V, R, ?, S, ?>) type;
	}

}
