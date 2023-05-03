package net.officefloor.cabinet.common.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodParametersMatcher;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.InvalidFieldValueException;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.CabinetManagerChange;
import net.officefloor.cabinet.common.RegisterDocumentMetaData;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldType;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;
import net.officefloor.cabinet.common.manage.DirtyInterceptor;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.manage.ManagedDocumentState;
import net.officefloor.cabinet.key.DocumentKey;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class DocumentMetaData<R, S, D, E, T> {

	/**
	 * Specified {@link Field} handling.
	 */
	private static class FieldValue<R, S, P, V> {

		private final Field field;

		private final FieldType<R, S, P, V> fieldType;

		private FieldValue(Field field, FieldType<R, S, P, V> fieldType) {
			this.field = field;
			this.fieldType = fieldType;
		}
	}

	/**
	 * {@link AbstractDocumentAdapter}.
	 */
	private final AbstractDocumentAdapter<R, S> adapter;

	/**
	 * {@link Document} type.
	 */
	public final Class<D> documentType;

	/**
	 * {@link ManagedDocument} type.
	 */
	private final Class<? extends D> managedDocumentType;

	/**
	 * {@link DocumentKey}.
	 */
	private final DocumentKey<D> documentKey;

	/**
	 * {@link FieldValue} instances.
	 */
	private final FieldValue<R, S, ?, ?>[] fieldValues;

	/**
	 * Top level {@link FieldValue} instances by {@link Field} name.
	 */
	private final Map<String, FieldValue<R, S, ?, ?>> fieldValuesByName;

	/**
	 * Extra meta-data specific to implementation.
	 */
	public final E extra;

	/**
	 * Instantiate the meta-data.
	 * 
	 * @param adapter      {@link AbstractDocumentAdapter}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances.
	 * @param officeStore  {@link AbstractOfficeStore}.
	 * @param register     {@link RegisterDocumentMetaData}.
	 * @throws Exception If fails to create abstract meta-data.
	 */
	public DocumentMetaData(AbstractDocumentAdapter<R, S> adapter, Class<D> documentType, Index[] indexes,
			AbstractOfficeStore<E, T> officeStore, RegisterDocumentMetaData<E, T> register) throws Exception {
		this.adapter = adapter;
		this.documentType = documentType;

		// Register this document meta-data
		if (register != null) {
			register.register(documentType, this);
		}

		// Obtain the document key
		this.documentKey = adapter.isDocument() ? CabinetUtil.getDocumentKey(documentType) : null;

		// Obtain and verify class loader
		ClassLoader classLoader = this.documentType.getClassLoader();
		classLoader.loadClass(DirtyInterceptor.class.getName());
		classLoader.loadClass(ManagedDocumentState.class.getName());
		classLoader.loadClass(ManagedDocument.class.getName());

		// Implement the managed document type
		this.managedDocumentType = new ByteBuddy().subclass(this.documentType)

				// Intercept setting methods to flag dirty
				.method(new MethodParametersMatcher<>((parameterList) -> parameterList.size() > 0))
				.intercept(MethodDelegation.to(DirtyInterceptor.class))

				// Ignore object and lombok methods
				.ignoreAlso(ElementMatchers.named("equals")).ignoreAlso(ElementMatchers.named("canEqual"))

				// Field maintaining dirty state
				.defineField(DirtyInterceptor.$$OfficeFloor$$_managedDocumentState, ManagedDocumentState.class,
						Modifier.PRIVATE)

				// ManagedDocument implementation
				.implement(ManagedDocument.class).intercept(FieldAccessor.ofBeanProperty())

				.make().load(this.documentType.getClassLoader()).getLoaded();

		// Register managed document type
		if (register != null) {
			register.register(this.managedDocumentType, this);
		}

		// Provide fields of document
		this.fieldValuesByName = new HashMap<>();
		List<FieldValue<R, S, ?, ?>> fieldValues = new ArrayList<>();
		CabinetUtil.processFields(this.documentType, (context) -> {

			// Ensure accessible
			Field field = context.getField();
			field.setAccessible(true);

			// Obtain type of field
			Class<?> fieldClass = field.getType();

			// Obtain the field type
			FieldType<R, S, ?, ?> fieldType = adapter.getFieldType(fieldClass);

			// Create and load the field value
			FieldValue<R, S, ?, ?> fieldValue = new FieldValue<>(field, fieldType);
			fieldValues.add(fieldValue);
			this.fieldValuesByName.put(field.getName(), fieldValue);
		});
		@SuppressWarnings("unchecked")
		FieldValue<R, S, ?, ?>[] typedFieldValues = fieldValues.toArray(FieldValue[]::new);
		this.fieldValues = typedFieldValues;

		// Create the extra meta-data
		this.extra = officeStore != null ? officeStore.createExtraMetaData(this, indexes) : null;
	}

	/**
	 * Obtains the {@link Key} name.
	 * 
	 * @return {@link Key} name.
	 */
	public String getKeyName() {
		return this.documentKey.getKeyName();
	}

	/**
	 * Obtains the {@link Key} from the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} for the {@link Document}.
	 */
	public String getDocumentKey(D document) {
		try {
			return this.documentKey.getKey(document);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to extract key from document of type "
					+ (document == null ? null : document.getClass().getName()), ex);
		}
	}

	/**
	 * Specifies the {@link Key} on the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @param key      {@link Key} for the {@link Document}.
	 */
	public void setDocumentKey(D document, String key) {
		try {
			this.documentKey.setKey(document, key);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to set key " + key + " on document of type "
					+ (document == null ? null : document.getClass().getName()), ex);
		}
	}

	/**
	 * Obtains the {@link Key} from the {@link Document} and loads a {@link Key} if
	 * not one specified.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} for the {@link Document}.
	 */
	public String getOrLoadDocumentKey(D document) {

		// Ensure document has a key
		String key = this.getDocumentKey(document);
		if (key == null) {

			// Generate the key
			key = CabinetUtil.newKey();

			// Load the key
			this.setDocumentKey(document, key);
		}

		// Return the key
		return key;
	}

	/**
	 * Obtains the {@link Key} from the {@link InternalDocument}.
	 * 
	 * @param internalDocument {@link InternalDocument}.
	 * @return {@link Key} for the {@link InternalDocument}.
	 */
	public String getInternalDocumentKey(R internalDocument) {
		return this.adapter.getKey(internalDocument, this.getKeyName());
	}

	/**
	 * Obtains the serialised field value from the {@link InternalDocument}.
	 * 
	 * @param fieldName        Name of the field.
	 * @param internalDocument {@link InternalDocument}.
	 * @return Value for the field.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String serialisedFieldValue(String fieldName, R internalDocument) {

		// Obtain the field meta-data
		FieldValue fieldValue = this.fieldValuesByName.get(fieldName);

		// Obtain the value of the field
		Object value = fieldValue.fieldType.getter.getValue(internalDocument, fieldName, null);

		// Return the serialised value of the field
		return fieldValue.fieldType.serialiser.getSerialisedValue(fieldName, value);
	}

	/**
	 * Obtains the deserialised field value for the {@link InternalDocument}.
	 * 
	 * @param fieldName Name of the field.
	 * @param value     Serialised value.
	 * @return Deserialised field value.
	 */
	public Object deserialisedFieldValue(String fieldName, String serialisedValue) {

		// Obtain the field meta-data
		FieldValue<R, S, ?, ?> fieldValue = this.fieldValuesByName.get(fieldName);

		// Return the deserialised value for the field
		return fieldValue.fieldType.deserialiser.getDeserialisedValue(fieldName, serialisedValue);
	}

	/**
	 * Creates the populated {@link ManagedDocument} from the internal
	 * {@link Document}.
	 * 
	 * @param internalDocument Internal {@link Document} containing data for the
	 *                         {@link ManagedDocument}.
	 * @param state            {@link ManagedDocumentState}.
	 * @return Populated {@link ManagedDocument}.
	 */
	public D createManagedDocument(R internalDocument, ManagedDocumentState state) {

		// Instantiate the document
		D document;
		try {
			document = this.managedDocumentType.getConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException("Should be able to create " + ManagedDocument.class.getSimpleName()
					+ " instance for " + this.documentType.getName(), ex);
		}

		// Load the state
		ManagedDocument managedDocument = (ManagedDocument) document;
		managedDocument.set$$OfficeFloor$$_managedDocumentState(state);

		// Determine if document to load it's key
		if (this.adapter.isDocument()) {

			// Load the document key
			String key = this.adapter.getKey(internalDocument, this.getKeyName());
			try {
				this.documentKey.setKey(document, key);
			} catch (Exception ex) {
				throw new IllegalStateException("Should be able to load key onto "
						+ ManagedDocument.class.getSimpleName() + " instance for " + this.documentType.getName(), ex);
			}
		}

		// Load the fields of the document
		for (FieldValue<R, S, ?, ?> fieldValue : this.fieldValues) {
			String fieldName = fieldValue.field.getName();

			// Obtain the value for the field
			Object value;
			try {
				value = fieldValue.fieldType.getter.getValue(internalDocument, fieldName, state);
			} catch (Exception ex) {
				throw new InvalidFieldValueException(this.documentType, fieldName, ex);
			}

			// Load value onto document (if value)
			if (value != null) {
				try {
					fieldValue.field.set(document, value);
				} catch (Exception ex) {
					throw new IllegalStateException("Should be able to load field " + fieldName + " of "
							+ ManagedDocument.class.getSimpleName() + " instance for " + this.documentType.getName(),
							ex);
				}
			}
		}

		// Return the document
		return document;
	}

	/**
	 * Creates the {@link StartAfterDocumentValueGetter} for the input
	 * {@link Document}.
	 *
	 * @param document {@link Document} containing data.
	 * @return {@link StartAfterDocumentValueGetter}.
	 */
	public StartAfterDocumentValueGetter createStartAfterDocumentValueGetter(D document) {
		return new StartAfterDocumentValueGetterImpl(document);
	}

	/**
	 * {@link StartAfterDocumentValueGetter} implementation.
	 */
	private class StartAfterDocumentValueGetterImpl implements StartAfterDocumentValueGetter {

		/**
		 * {@link Document}.
		 */
		private final D document;

		/**
		 * Instantiate.
		 * 
		 * @param document {@link Document}.
		 */
		private StartAfterDocumentValueGetterImpl(D document) {
			this.document = document;
		}

		/*
		 * ==================== StartAfterDocumentValueGetter ====================
		 */

		@Override
		public String getKeyFieldName() {
			return DocumentMetaData.this.documentKey.getKeyName();
		}

		@Override
		public String getKey() {
			return DocumentMetaData.this.getDocumentKey(this.document);
		}

		@Override
		public Object getValue(String fieldName) {

			// Obtain the field value
			FieldValue<R, S, ?, ?> fieldValue = DocumentMetaData.this.fieldValuesByName.get(fieldName);
			if (fieldValue == null) {
				return null; // must find field to have value
			}

			// Retrieve the value
			Object value;
			try {
				value = fieldValue.field.get(this.document);
			} catch (Exception ex) {
				throw new IllegalStateException("Failed to extract value for field " + fieldName
						+ " from document of type " + (document == null ? null : document.getClass().getName()), ex);
			}

			// Transform to persistence value
			@SuppressWarnings("unchecked")
			FieldValueTranslator<Object, ?> translator = (FieldValueTranslator<Object, ?>) fieldValue.fieldType.translator;
			Object persistenceValue = translator.translate(fieldName, value);

			// Return the persistence value
			return persistenceValue;
		}
	}

	/**
	 * Creates the {@link InternalDocument} to store.
	 * 
	 * @param document {@link Document} containing data.
	 * @param change   {@link CabinetManagerChange}.
	 * @return Populated {@link InternalDocument}.
	 */
	public InternalDocument<S> createInternalDocument(D document, CabinetManagerChange change) {

		// Create the internal document
		S internalDocument = this.adapter.createInternalDocument();

		// Determine key and whether new
		String key;
		boolean isNew = false;
		if (this.adapter.isDocument()) {

			// Obtain the key for the document
			try {
				key = this.documentKey.getKey(document);
			} catch (Exception ex) {
				throw new IllegalStateException(
						"Unable to obtain key from " + (document == null ? null : document.getClass().getName())
								+ " (of expected type " + this.documentType.getName() + ")",
						ex);
			}

			// No key, so new
			if (key == null) {

				// Generate and load key
				key = CabinetUtil.newKey();
				try {
					this.documentKey.setKey(document, key);
				} catch (Exception ex) {
					throw new IllegalStateException(
							"Unable to specify key on " + (document == null ? null : document.getClass().getName())
									+ " (of expected type " + this.documentType.getName() + ")",
							ex);
				}

				// Flag creating
				isNew = true;
			}

			// Specify key on internal document
			this.adapter.setStoreKey(internalDocument, this.getKeyName(), key);

		} else {
			// Section of document
			key = null;
			isNew = false;
		}

		// Load the field values into internal document
		for (FieldValue<R, S, ?, ?> fieldValue : this.fieldValues) {
			String fieldName = fieldValue.field.getName();

			// Obtain the value
			Object value;
			try {
				value = fieldValue.field.get(document);
			} catch (Exception ex) {
				throw new IllegalStateException("Should be able to obtain field " + this.documentType.getName() + "#"
						+ fieldName + " from " + (document == null ? null : document.getClass().getName()), ex);
			}

			// Transform to persistence value
			Object persistenceValue;
			try {
				@SuppressWarnings("unchecked")
				FieldValueTranslator<Object, ?> translator = (FieldValueTranslator<Object, ?>) fieldValue.fieldType.translator;
				persistenceValue = translator.translate(fieldName, value);
			} catch (Exception ex) {
				throw new IllegalStateException("Should be able to transform field " + this.documentType.getName() + "#"
						+ fieldName + " of type " + (value == null ? null : value.getClass().getName())
						+ " for interal document", ex);
			}

			// TODO store value or just retrieve key

			// Load value into internal document
			try {
				@SuppressWarnings("unchecked")
				FieldValueSetter<S, Object> setter = (FieldValueSetter<S, Object>) fieldValue.fieldType.setter;
				setter.setValue(internalDocument, fieldName, persistenceValue, change);
			} catch (Exception ex) {
				throw new IllegalStateException(
						"Should be able to load field " + this.documentType.getName() + "#" + fieldName + " of type "
								+ (persistenceValue == null ? null : persistenceValue.getClass().getName())
								+ " to internal document",
						ex);
			}
		}

		// Return the internal document
		return new InternalDocument<>(key, internalDocument, isNew);
	}

}