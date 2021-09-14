package net.officefloor.cabinet.common.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodParametersMatcher;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.InvalidFieldValueException;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldType;
import net.officefloor.cabinet.common.key.DocumentKey;
import net.officefloor.cabinet.common.manage.DirtyInterceptor;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.manage.ManagedDocumentState;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentMetaData<R, S, A extends AbstractDocumentAdapter<R, S, A>, D> {

	/**
	 * Specified {@link Field} handling.
	 */
	private static class FieldValue<R, S, V> {

		private final Field field;

		private final FieldType<R, S, V> fieldType;

		private FieldValue(Field field, FieldType<R, S, V> fieldType) {
			this.field = field;
			this.fieldType = fieldType;
		}
	}

	/**
	 * {@link AbstractDocumentAdapter}.
	 */
	private final A adapter;

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
	private final FieldValue<R, S, ?>[] fieldValues;

	/**
	 * Instantiate the meta-data.
	 * 
	 * @param adapter      {@link AbstractDocumentAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create abstract meta-data.
	 */
	public AbstractDocumentMetaData(A adapter, Class<D> documentType) throws Exception {
		this.adapter = adapter;
		this.documentType = documentType;

		// Obtain the document key
		this.documentKey = adapter.isDocument() ? CabinetUtil.getDocumentKey(documentType) : null;

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

		// Provide fields of document
		List<FieldValue<R, S, ?>> fieldValues = new ArrayList<>();
		CabinetUtil.processFields(this.documentType, (context) -> {

			// Ignore key
			if (context.isKey()) {
				return;
			}

			// Ensure accessible
			Field field = context.getField();
			field.setAccessible(true);

			// Obtain type of field
			Class<?> fieldClass = field.getType();

			// Obtain the field type
			FieldType<R, S, ?> fieldType = adapter.getFieldType(fieldClass);

			// Create and load the field value
			FieldValue<R, S, ?> fieldValue = new FieldValue<>(field, fieldType);
			fieldValues.add(fieldValue);
		});
		@SuppressWarnings("unchecked")
		FieldValue<R, S, ?>[] typedFieldValues = fieldValues.toArray(FieldValue[]::new);
		this.fieldValues = typedFieldValues;
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
	 * Obtains the {@link Key} from the internal {@link Document}.
	 * 
	 * @param internalDocument Internal {@link Document}.
	 * @return {@link Key} for the internal {@link Document}.
	 */
	public String getKey(R internalDocument) {
		return this.adapter.getKey(internalDocument, this.getKeyName());
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
		for (FieldValue<R, S, ?> fieldValue : this.fieldValues) {
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
	 * Creates the internal {@link Document}.
	 * 
	 * @param document {@link Document} containing data.
	 * @return Populated {@link InternalDocument}.
	 */
	public InternalDocument<S> createInternalDocumnet(D document) {

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
			this.adapter.setKey(internalDocument, this.getKeyName(), key);

		} else {
			// Section of document
			key = null;
			isNew = false;
		}

		// Load the field values into internal document
		for (FieldValue<R, S, ?> fieldValue : this.fieldValues) {
			String fieldName = fieldValue.field.getName();

			// Obtain the value
			Object value;
			try {
				value = fieldValue.field.get(document);
			} catch (Exception ex) {
				throw new IllegalStateException("Should be able to obtain field " + this.documentType.getName() + "#"
						+ fieldName + " from " + (document == null ? null : document.getClass().getName()), ex);
			}

			// Load value into internal document
			try {
				@SuppressWarnings("unchecked")
				FieldValue<R, S, Object> typedFieldValue = (FieldValue<R, S, Object>) fieldValue;
				typedFieldValue.fieldType.setter.setValue(internalDocument, fieldName, value);
			} catch (Exception ex) {
				throw new IllegalStateException(
						"Should be able to load field " + this.documentType.getName() + "#" + fieldName + " of type "
								+ (value == null ? null : value.getClass().getName()) + " to interal document",
						ex);
			}
		}

		// Return the internal document
		return new InternalDocument<>(key, internalDocument, isNew);
	}

}