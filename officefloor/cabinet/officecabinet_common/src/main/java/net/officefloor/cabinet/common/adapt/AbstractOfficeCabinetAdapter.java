package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying store implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetAdapter<R, S> {

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
	 * @param adapter    {@link AbstractOfficeCabinetAdapter}.
	 * @param fieldTypes {@link Field} types.
	 */
	private static <R, S> void assertFieldTypes(AbstractOfficeCabinetAdapter<R, S> adapter, Class<?>... fieldTypes) {
		for (Class<?> fieldType : fieldTypes) {
			assertInitialise(() -> !adapter.fieldTypes.containsKey(fieldType),
					"Must initialise field type " + fieldType.getName());
		}
	}

	/**
	 * Means to initialise to typed internal {@link Document}.
	 */
	protected class Initialise {

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
			AbstractOfficeCabinetAdapter.this.internalDocumentFactory = internalDocumentFactory;
		}

		/**
		 * Specifies the {@link KeyGetter}.
		 * 
		 * @param keyGetter {@link KeyGetter}.
		 */
		public void setKeyGetter(KeyGetter<R> keyGetter) {
			AbstractOfficeCabinetAdapter.this.keyGetter = keyGetter;
		}

		/**
		 * Specifies the {@link KeySetter}.
		 * 
		 * @param keySetter {@link KeySetter}.
		 */
		public void setKeySetter(KeySetter<S> keySetter) {
			AbstractOfficeCabinetAdapter.this.keySetter = keySetter;
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
			AbstractOfficeCabinetAdapter.this.fieldTypes.put(fieldType, new FieldType<>(getter, setter));
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
	private final Map<Class<?>, FieldType<R, S, ?>> fieldTypes = new HashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @throws Exception If fails creating adapter.
	 */
	public AbstractOfficeCabinetAdapter() {

		// Initialise
		try {
			this.initialise(new Initialise());
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialise " + AbstractOfficeCabinetAdapter.class.getSimpleName()
					+ " implementation " + this.getClass().getName(), ex);
		}

		// Ensure internal document factory initialised
		assertInitialise(() -> internalDocumentFactory == null,
				"Must specify " + InternalDocumentFactory.class.getSimpleName());

		// Ensure keys initialised
		assertInitialise(() -> keyGetter == null, "Must specify " + KeyGetter.class.getSimpleName());
		assertInitialise(() -> keySetter == null, "Must specify " + KeySetter.class.getSimpleName());

		// Ensure primitives initialised
		assertFieldTypes(this, boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class,
				char.class, Character.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
				double.class, Double.class);

		// Ensure open types initialised
		assertFieldTypes(this, String.class);
	}

	/**
	 * Initialises static meta-data.
	 * 
	 * @param init {@link Initialise}.
	 * @throws Exception If fails to load the {@link Initialise}.
	 */
	protected abstract void initialise(Initialise init) throws Exception;

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
		if (fieldType == null) {
			// TODO load as embedded type
			throw new UnsupportedOperationException("TODO implement embedded for type " + fieldType.getName());
		}

		// Return the field type
		return (FieldType<R, S, V>) type;
	}

}
