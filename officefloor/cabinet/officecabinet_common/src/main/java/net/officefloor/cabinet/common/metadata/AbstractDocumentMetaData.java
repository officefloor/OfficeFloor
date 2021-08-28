package net.officefloor.cabinet.common.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder.ParameterBinder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodParametersMatcher;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.InvalidFieldValueException;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.adapt.AbstractOfficeCabinetAdapter;
import net.officefloor.cabinet.common.adapt.FieldType;
import net.officefloor.cabinet.common.key.DocumentKey;
import net.officefloor.cabinet.common.manage.DirtyInterceptor;
import net.officefloor.cabinet.common.manage.DirtyInterceptor.ManagedDocumentField;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.manage.ManagedDocumentState;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentMetaData<R, S, D> {

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
	 * {@link AbstractOfficeCabinetAdapter}.
	 */
	private final AbstractOfficeCabinetAdapter<R, S> adapter;

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
	 * @param adapter      {@link AbstractOfficeCabinetAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create abstract meta-data.
	 */
	public AbstractDocumentMetaData(AbstractOfficeCabinetAdapter<R, S> adapter, Class<D> documentType)
			throws Exception {
		this.adapter = adapter;
		this.documentType = documentType;

		// Obtain the document key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);

		// Implement the managed document type
		ParameterBinder<FieldProxy> stateField = FieldProxy.Binder.install(ManagedDocumentField.class);
		this.managedDocumentType = new ByteBuddy().subclass(this.documentType)

				// Intercept setting methods to flag dirty
				.method(new MethodParametersMatcher<>((parameterList) -> parameterList.size() > 0))
				.intercept(MethodDelegation.to(DirtyInterceptor.FlagDirty.class))

				// Ignore object and lombok methods
				.ignoreAlso(ElementMatchers.named("equals")).ignoreAlso(ElementMatchers.named("canEqual"))

				// Field maintaining dirty state
				.defineField(DirtyInterceptor.$$OfficeFloor$$_getManagedDocumentState, ManagedDocumentState.class,
						Modifier.PRIVATE)

				// Constructor to default the dirty state
				.constructor(ElementMatchers.isDefaultConstructor())
				.intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration()
						.withBinders(stateField).to(DirtyInterceptor.DefaultConstructor.class)))

				// Interface for managing the document
				.implement(ManagedDocument.class)

				.defineMethod(DirtyInterceptor.$$OfficeFloor$$_getManagedDocumentState, ManagedDocumentState.class,
						Modifier.PUBLIC)
				.intercept(MethodDelegation.withDefaultConfiguration().withBinders(stateField)
						.to(DirtyInterceptor.ManagedDocumentImpl.class))

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
	 * Creates the populated {@link ManagedDocument} from the internal
	 * {@link Document}.
	 * 
	 * @param internalDocument Internal {@link Document} containing data for the
	 *                         {@link ManagedDocument}.
	 * @return Populated {@link ManagedDocument}.
	 */
	public D createManagedDocument(R internalDocument) {

		// Instantiate the document
		D document;
		try {
			document = this.managedDocumentType.getConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException("Should be able to create " + ManagedDocument.class.getSimpleName()
					+ " instance for " + this.documentType.getName(), ex);
		}

		// Load the document key
		String key = this.adapter.getKey(internalDocument, this.getKeyName());
		try {
			this.documentKey.setKey(document, key);
		} catch (Exception ex) {
			throw new IllegalStateException("Should be able to load key onto " + ManagedDocument.class.getSimpleName()
					+ " instance for " + this.documentType.getName(), ex);
		}

		// Load the fields of the document
		for (FieldValue<R, S, ?> fieldValue : this.fieldValues) {
			String fieldName = fieldValue.field.getName();

			// Obtain the value for the field
			Object value;
			try {
				value = fieldValue.fieldType.getter.getValue(internalDocument, fieldName);
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

		// Obtain the key for the document
		String key;
		try {
			key = this.documentKey.getKey(document);
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Unable to obtain key from " + (document == null ? null : document.getClass().getName())
							+ " (of expected type " + this.documentType.getName() + ")",
					ex);
		}

		// No key, so new
		boolean isNew = false;
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

		// Create the internal document
		S internalDocument = this.adapter.createInternalDocument();

		// Specify key on internal document
		this.adapter.setKey(internalDocument, this.getKeyName(), key);

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