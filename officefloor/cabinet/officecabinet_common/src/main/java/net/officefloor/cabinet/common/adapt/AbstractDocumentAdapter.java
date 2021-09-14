package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying store implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentAdapter<R, S, A extends AbstractDocumentAdapter<R, S, A>> {

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
		 * @param <V>       Type of {@link Field} value.
		 * @param fieldType Type of {@link Field} value.
		 * @param getter    {@link FieldValueGetter}.
		 * @param setter    {@link FieldValueSetter}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <V> void addFieldType(Class<V> fieldType, ScalarFieldValueGetter<R, V> getter,
				FieldValueSetter<S, V> setter) {
			FieldValueGetter<R, V> adaptGetter = (doc, fieldName, state) -> getter.getValue(doc, fieldName);
			if (Map.class.equals(fieldType)) {
				AbstractDocumentAdapter.this.mapFieldType = new FieldType(adaptGetter, setter);
			} else {
				AbstractDocumentAdapter.this.fieldTypes.put(fieldType, new FieldType<>(adaptGetter, setter));
			}
		}

		/**
		 * Adds a {@link FieldType}.
		 * 
		 * @param <V>            Type of the {@link Field} value.
		 * @param fieldType      Primitive type of the {@link Field} value.
		 * @param boxedFieldType Auto-boxed type for {@link Field}.
		 * @param getter         {@link FieldValueGetter}.
		 * @param setter         {@link FieldValueSetter}.
		 */
		public <V> void addFieldType(Class<V> fieldType, Class<V> boxedFieldType, ScalarFieldValueGetter<R, V> getter,
				FieldValueSetter<S, V> setter) {
			this.addFieldType(fieldType, getter, setter);
			this.addFieldType(boxedFieldType, getter, setter);
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
	private final Map<Class<?>, FieldType<R, S, ?>> fieldTypes = new HashMap<>();

	/**
	 * {@link Map} {@link FieldType}.
	 */
	private FieldType<R, S, Map<String, Object>> mapFieldType;

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
	 * @param adapter    {@link AbstractDocumentAdapter}.
	 * @param fieldTypes {@link Field} types.
	 */
	private void assertFieldTypes(Class<?>... fieldTypes) {
		for (Class<?> fieldType : fieldTypes) {
			assertInitialise(() -> !this.fieldTypes.containsKey(fieldType),
					"Must initialise field type " + fieldType.getName());
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
	 * @return {@link AbstractDocumentMetaData}.
	 * @throws Exception If fails to create {@link AbstractDocumentMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public <D> AbstractDocumentMetaData<R, S, A, D> createDocumentMetaData(Class<D> documentType) throws Exception {
		return this.documentMetaDataFactory.createDocumentMetaData(documentType, (A) this);
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
	 * Specifies the {@link Key} value.
	 * 
	 * @param internalDocument Stored internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @param keyValue         {@link Key} value.
	 */
	public void setKey(S internalDocument, String keyName, String keyValue) {
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
	public <V> FieldType<R, S, V> getFieldType(Class<V> fieldType) {

		// Obtain the type
		FieldType<R, S, ?> type = this.fieldTypes.get(fieldType);

		// Object type, so determine load
		if (type == null) {

			// Create the section meta-data
			AbstractDocumentMetaData<Map<String, Object>, Map<String, Object>, ?, V> sectionMetaData;
			try {
				sectionMetaData = this.sectionAdapter.createDocumentMetaData(fieldType);
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to create document meta-data for " + fieldType.getName(), ex);
			}

			// Create type for non-handled
			type = new FieldType<R, S, V>((internalDocument, fieldName, state) -> {

				// Get sub section retrieved
				Map<String, Object> section = this.mapFieldType.getter.getValue(internalDocument, fieldName, state);

				// With retrieved, create managed document
				V managedDocument = section != null ? sectionMetaData.createManagedDocument(section, state) : null;

				// Return the managed document
				return managedDocument;

			}, (internalDocument, fieldName, value) -> {

				// Create section
				Map<String, Object> section = value != null
						? sectionMetaData.createInternalDocumnet(value).getInternalDocument()
						: null;

				// Assign to input internal document
				this.mapFieldType.setter.setValue(internalDocument, fieldName, section);
			});
		}

		// Return the field type
		return (FieldType<R, S, V>) type;
	}

}
