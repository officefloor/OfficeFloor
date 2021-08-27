package net.officefloor.cabinet.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
import net.officefloor.cabinet.common.DirtyInterceptor.ManagedDocumentField;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDocumentMetaData<R, S, D> {

	/**
	 * Creates the internal {@link Document}.
	 */
	@FunctionalInterface
	protected static interface InternalDocumentFactory<S> {
		S createInternalDocument();
	}

	/**
	 * Obtains the {@link Key} for the internal {@link Document}.
	 */
	@FunctionalInterface
	protected static interface KeyGetter<R> {
		String getKey(R internalDocument, String keyName);
	}

	/**
	 * Specifies the {@link Key} on the internal {@link Document}.
	 */
	@FunctionalInterface
	protected static interface KeySetter<S> {
		void setKey(S internalDocument, String keyName, String keyValue);
	}

	/**
	 * Obtains the {@link Field} value from internal {@link Document}.
	 */
	@FunctionalInterface
	protected static interface FieldValueGetter<R, V> {
		V getValue(R internalDocument, String fieldName);
	}

	/**
	 * Loads the {@link Field} value onto the internal {@link Document}.
	 */
	@FunctionalInterface
	protected static interface FieldValueSetter<S, V> {
		void setValue(S internalDocument, String fieldName, V value);
	}

	/**
	 * Meta-data of {@link Field}.
	 */
	private static class FieldType<R, S, V> {

		private final FieldValueGetter<R, V> getter;

		private final FieldValueSetter<S, V> setter;

		private FieldType(FieldValueGetter<R, V> getter, FieldValueSetter<S, V> setter) {
			this.getter = getter;
			this.setter = setter;
		}
	}

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
	 * {@link InternalDocumentFactory}.
	 */
	private static InternalDocumentFactory<?> internalDocumentFactory;

	/**
	 * {@link KeyGetter}.
	 */
	private static KeyGetter<?> keyGetter;

	/**
	 * {@link KeySetter}.
	 */
	private static KeySetter<?> keySetter;

	/**
	 * Mapping of {@link Field} type to {@link FieldType}.
	 */
	private static final Map<Class<?>, FieldType<?, ?, ?>> fieldTypes = new HashMap<>();

	/**
	 * Means to initialise to typed internal {@link Document}.
	 */
	protected static class Initialise<R, S> {

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
			AbstractDocumentMetaData.internalDocumentFactory = internalDocumentFactory;
		}

		/**
		 * Specifies the {@link KeyGetter}.
		 * 
		 * @param keyGetter {@link KeyGetter}.
		 */
		public void setKeyGetter(KeyGetter<R> keyGetter) {
			AbstractDocumentMetaData.keyGetter = keyGetter;
		}

		/**
		 * Specifies the {@link KeySetter}.
		 * 
		 * @param keySetter {@link KeySetter}.
		 */
		public void setKeySetter(KeySetter<S> keySetter) {
			AbstractDocumentMetaData.keySetter = keySetter;
		}

		/**
		 * Adds a {@link FieldType}.
		 * 
		 * @param <V>       Type of {@link Field} value.
		 * @param fieldType Type of {@link Field} value.
		 * @param getter    {@link FieldValueGetter}.
		 * @param setter    {@link FieldValueSetter}.
		 */
		public <V> void addFieldType(Class<V> fieldType, FieldValueGetter<R, V> getter, FieldValueSetter<S, V> setter) {
			fieldTypes.put(fieldType, new FieldType<>(getter, setter));
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
		public <V> void addFieldType(Class<V> fieldType, Class<V> boxedFieldType, FieldValueGetter<R, V> getter,
				FieldValueSetter<S, V> setter) {
			this.addFieldType(fieldType, getter, setter);
			this.addFieldType(boxedFieldType, getter, setter);
		}
	}

	/**
	 * Asserts appropriate initialise.
	 * 
	 * @param assertion Assertion.
	 * @param message   Message.
	 */
	private static void assertInitialise(Supplier<Boolean> assertion, String message) {
		if (assertion.get()) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Asserts appropriate {@link Field} types have been initialised.
	 * 
	 * @param fieldTypes {@link Field} types.
	 */
	private static void assertFieldTypes(Class<?>... fieldTypes) {
		for (Class<?> fieldType : fieldTypes) {
			assertInitialise(() -> !AbstractDocumentMetaData.fieldTypes.containsKey(fieldType),
					"Must initialise field type " + fieldType.getName());
		}
	}

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
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create abstract meta-data.
	 */
	public AbstractDocumentMetaData(Class<D> documentType) throws Exception {

		// Ensure initialised
		if (keySetter == null) {
			this.initialise(new Initialise<>());

			// Ensure internal document factory initialised
			assertInitialise(() -> internalDocumentFactory == null,
					"Must specify " + InternalDocumentFactory.class.getSimpleName());

			// Ensure keys initialised
			assertInitialise(() -> keyGetter == null, "Must specify " + KeyGetter.class.getSimpleName());
			assertInitialise(() -> keySetter == null, "Must specify " + KeySetter.class.getSimpleName());

			// Ensure primitives initialised
			assertFieldTypes(boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class, char.class,
					Character.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
					double.class, Double.class);

			// Ensure open types initialised
			assertFieldTypes(String.class);
		}

		// Specify document type
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

			// Determine the field type
			@SuppressWarnings("unchecked")
			FieldType<R, S, ?> fieldType = (FieldType<R, S, ?>) fieldTypes.get(fieldClass);
			if (fieldType == null) {
				// TODO load as embedded type
				throw new UnsupportedOperationException(
						"TODO implement embedded for " + field.getName() + " of type " + fieldClass.getName());
			}

			// Create and load the field value
			FieldValue<R, S, ?> fieldValue = new FieldValue<>(field, fieldType);
			fieldValues.add(fieldValue);
		});
		@SuppressWarnings("unchecked")
		FieldValue<R, S, ?>[] typedFieldValues = fieldValues.toArray(FieldValue[]::new);
		this.fieldValues = typedFieldValues;
	}

	/**
	 * Initialises static meta-data.
	 * 
	 * @param init {@link Initialise}.
	 * @throws Exception If fails to load the {@link Initialise}.
	 */
	protected abstract void initialise(Initialise<R, S> init) throws Exception;

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
		@SuppressWarnings("unchecked")
		KeyGetter<R> typedKeyGetter = (KeyGetter<R>) keyGetter;
		String key = typedKeyGetter.getKey(internalDocument, this.getKeyName());
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
		@SuppressWarnings("unchecked")
		InternalDocumentFactory<S> typedInternalDocumentFactory = (InternalDocumentFactory<S>) internalDocumentFactory;
		S internalDocument = typedInternalDocumentFactory.createInternalDocument();

		// Specify key on internal document
		@SuppressWarnings("unchecked")
		KeySetter<S> typedKeySetter = (KeySetter<S>) keySetter;
		typedKeySetter.setKey(internalDocument, this.getKeyName(), key);

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