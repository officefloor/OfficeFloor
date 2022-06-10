package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying store implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentAdapter<R, S, A extends AbstractDocumentAdapter<R, S, A>> {

	/**
	 * Transforms the field value for the {@link Map}.
	 */
	@FunctionalInterface
	public static interface FieldValueTransform<I, O> {
		O transform(I inputValue);
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
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueDeserialiser}.
	 */
	public static <V> FieldValueDeserialiser<V> notDeserialiseable() {
		return (fieldName, serialisedValue) -> {
			throw new UnsupportedOperationException(
					"Should not be deserialising field " + fieldName + " of type " + Map.class.getName());
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
		 * Specifies the {@link DocumentMetaDataFactory}.
		 * 
		 * @param documentMetaDataFactory {@link DocumentMetaDataFactory}.
		 */
		public void setDocumentMetaDataFactory(DocumentMetaDataFactory<R, S, A> documentMetaDataFactory) {
			AbstractDocumentAdapter.this.documentMetaDataFactory = documentMetaDataFactory;
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
		 * Adds a {@link FieldType}.
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
			FieldValueGetter<R, V> adaptGetter = (doc, fieldName, state) -> getter.getValue(doc, fieldName);
			if (Map.class.equals(fieldType)) {
				AbstractDocumentAdapter.this.mapFieldType = new FieldType(adaptGetter, translator, setter, serialiser,
						deserialiser);
			} else {
				AbstractDocumentAdapter.this.fieldTypes.put(fieldType,
						new FieldType<>(adaptGetter, translator, setter, serialiser, deserialiser));
			}
		}

		/**
		 * Adds a {@link FieldType}.
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
			this.addFieldType(fieldType, getter, translator, setter, serialiser, deserialiser);
			this.addFieldType(boxedFieldType, getter, translator, setter, serialiser, deserialiser);
		}
	}

	/**
	 * {@link AbstractSectionAdapter}.
	 */
	private final AbstractSectionAdapter<?> sectionAdapter;

	/**
	 * Indicates if the top level {@link Document}.
	 */
	private final boolean isDocument;

	/**
	 * {@link InternalDocumentFactory}.
	 */
	private InternalDocumentFactory<S> internalDocumentFactory;

	/**
	 * {@link DocumentMetaDataFactory}.
	 */
	private DocumentMetaDataFactory<R, S, A> documentMetaDataFactory;

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
	private final Map<Class<?>, FieldType<R, S, ?, ?>> fieldTypes = new HashMap<>();

	/**
	 * {@link Map} {@link FieldType}.
	 */
	private FieldType<R, S, Map<String, Object>, Map<String, Object>> mapFieldType;

	/**
	 * Instantiate as {@link Document}.
	 * 
	 * @param sectionAdapter {@link AbstractSectionAdapter}.
	 * @throws Exception If fails creating adapter.
	 */
	public AbstractDocumentAdapter(AbstractSectionAdapter<?> sectionAdapter) {
		this(true, sectionAdapter);
	}

	/**
	 * Instantiate.
	 * 
	 * @param sectionAdapter {@link AbstractSectionAdapter}.
	 * @param isSection      Indicates if top level document.
	 */
	@SuppressWarnings("unchecked")
	AbstractDocumentAdapter(boolean isDocument, AbstractSectionAdapter<?> sectionAdapter) {
		this.isDocument = isDocument;

		// Section adapter (allows recursive section adapting)
		this.sectionAdapter = sectionAdapter != null ? sectionAdapter : (AbstractSectionAdapter<?>) this;

		// Load default initialise
		Initialise init = new Initialise();
		this.defaultInitialise(init);

		// Initialise
		try {
			this.initialise(init);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialise " + AbstractDocumentAdapter.class.getSimpleName()
					+ " implementation " + this.getClass().getName(), ex);
		}

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

		// Ensure document meta-data factory initialised
		this.assertInitialise(() -> this.documentMetaDataFactory == null,
				"Must specify " + DocumentMetaDataFactory.class.getSimpleName());

		// Ensure primitives initialised
		this.assertFieldTypes(boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class,
				char.class, Character.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
				double.class, Double.class);

		// Ensure open types initialised
		this.assertFieldTypes(String.class);

		// Ensure hierachy loaded
		this.assertInitialise(() -> this.mapFieldType == null, "Must initialise field type " + Map.class.getName());
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
			FieldType<R, S, ?, ?> fieldType = this.fieldTypes.get(type);
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
	 * Creates the {@link AbstractDocumentMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @return {@link AbstractDocumentMetaData}.
	 * @throws Exception If fails to create {@link AbstractDocumentMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public <D> AbstractDocumentMetaData<R, S, A, D> createDocumentMetaData(Class<D> documentType, Index[] indexes)
			throws Exception {
		return this.documentMetaDataFactory.createDocumentMetaData(documentType, indexes, (A) this);
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
	 * Specifies the {@link Key} value on {@link InternalDocument} for retrieving.
	 * 
	 * @param internalDocument Retrieved internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @param keyValue         {@link Key} value.
	 */
	public void setRetrieveKey(R internalDocument, String keyName, String keyValue) {

		// TODO REMOVE
		throw new UnsupportedOperationException("TODO implement");
	}

	/**
	 * Obtains the {@link FieldType} for the {@link Field} type.
	 * 
	 * @param <V>       Type of {@link Field}.
	 * @param fieldType {@link Field} type.
	 * @return {@link FieldType}.
	 */
	@SuppressWarnings("unchecked")
	public <V> FieldType<R, S, ?, V> getFieldType(Class<V> fieldType) {

		// Obtain the type
		FieldType<R, S, ?, ?> type = this.fieldTypes.get(fieldType);

		// Object type, so determine load
		if (type == null) {

			// Create the section meta-data
			AbstractDocumentMetaData<Map<String, Object>, Map<String, Object>, ?, V> sectionMetaData;
			try {
				sectionMetaData = this.sectionAdapter.createDocumentMetaData(fieldType, new Index[0]);
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to create document meta-data for " + fieldType.getName(), ex);
			}

			// Create type for non-handled
			FieldValueGetter<R, V> getter = (internalDocument, fieldName, state) -> {

				// Get sub section retrieved
				Map<String, Object> section = this.mapFieldType.getter.getValue(internalDocument, fieldName, state);

				// With retrieved, create managed document
				V managedDocument = section != null ? sectionMetaData.createManagedDocument(section, state) : null;

				// Return the managed document
				return managedDocument;

			};
			FieldValueTranslator<V, V> translator = (fieldName, value) -> value;
			FieldValueSetter<S, V> setter = (internalDocument, fieldName, value) -> {

				// Create section
				Map<String, Object> section = value != null
						? sectionMetaData.createInternalDocument(value).getInternalDocument()
						: null;

				// Assign to input internal document
				this.mapFieldType.setter.setValue(internalDocument, fieldName, section);
			};
			type = new FieldType<R, S, V, V>(getter, translator, setter, (fieldName, fieldValue) -> {
				throw new UnsupportedOperationException(
						"Can not serialise Object into next bundle token for field " + fieldName);
			}, (fieldName, serialisedValue) -> {
				throw new UnsupportedOperationException("Can not deserialise Object from next bundle token");
			});
		}

		// Return the field type
		return (FieldType<R, S, ?, V>) type;
	}

}
