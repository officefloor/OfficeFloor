package net.officefloor.cabinet.cosmosdb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeCabinetMetaData;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Meta-data for the {@link CosmosOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosOfficeCabinetMetaData<D> extends AbstractOfficeCabinetMetaData<D> {

	/**
	 * Mapping of {@link Field} type to property type.
	 */
	private static final Map<Class<?>, PropertyType<?>> fieldTypeToPropertyType = new HashMap<>();

	private static <T> void addFieldTypeToPropertyType(Class<T> fieldType, PropertyGetter<T> getter,
			PropertySetter<T> setter) {
		fieldTypeToPropertyType.put(fieldType, new PropertyType<>(getter, setter));
	}

	private static <T> void addFieldTypeToPropertyType(Class<T> fieldType, Class<T> boxedFieldType,
			PropertyGetter<T> getter, PropertySetter<T> setter) {
		addFieldTypeToPropertyType(fieldType, getter, setter);
		addFieldTypeToPropertyType(boxedFieldType, getter, setter);
	}

	static {
		addFieldTypeToPropertyType(boolean.class, Boolean.class, InternalObjectNode::getBoolean,
				InternalObjectNode::set);
		addFieldTypeToPropertyType(byte.class, Byte.class, (doc, property) -> {
			Integer value = doc.getInt(property);
			return value != null ? value.byteValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Integer.valueOf(value) : null));
		addFieldTypeToPropertyType(short.class, Short.class, (doc, property) -> {
			Integer value = doc.getInt(property);
			return value != null ? value.shortValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Integer.valueOf(value) : null));
		addFieldTypeToPropertyType(char.class, Character.class, (item, attributeName) -> {
			String value = item.getString(attributeName);
			return value != null ? value.charAt(0) : null;
		}, (doc, property, value) -> doc.set(property, value != null ? new String(new char[] { value }) : null));
		addFieldTypeToPropertyType(int.class, Integer.class, InternalObjectNode::getInt, InternalObjectNode::set);
		addFieldTypeToPropertyType(long.class, Long.class, (doc, property) -> {
			String value = doc.getString(property);
			return value != null ? Long.parseLong(value) : null;
		}, (doc, property, value) -> {
			String text = value != null ? String.valueOf(value) : null;
			doc.set(property, text);
		});
		addFieldTypeToPropertyType(float.class, Float.class, (doc, property) -> {
			Double value = doc.getDouble(property);
			return value != null ? value.floatValue() : null;
		}, (doc, property, value) -> doc.set(property, value != null ? Double.valueOf(value) : null));
		addFieldTypeToPropertyType(double.class, Double.class, InternalObjectNode::getDouble, InternalObjectNode::set);

		addFieldTypeToPropertyType(String.class, InternalObjectNode::getString, InternalObjectNode::set);
	}

	@FunctionalInterface
	static interface PropertyGetter<T> {
		T get(InternalObjectNode document, String propertyName);
	}

	@FunctionalInterface
	static interface PropertySetter<T> {
		void set(InternalObjectNode document, String propertyName, T value);
	}

	static class PropertyType<T> {

		final PropertyGetter<T> getter;

		final PropertySetter<T> setter;

		private PropertyType(PropertyGetter<T> getter, PropertySetter<T> setter) {
			this.getter = getter;
			this.setter = setter;
		}
	}

	static class Property<T> {

		final Field field;

		final PropertyType<T> propertyType;

		private Property(Field field, PropertyType<T> propertyType) {
			this.field = field;
			this.propertyType = propertyType;
		}
	}

	/**
	 * {@link CosmosContainer}.
	 */
	final CosmosContainer container;

	/**
	 * {@link Property} instances for {@link Document}.
	 */
	final Property<?>[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	CosmosOfficeCabinetMetaData(Class<D> documentType, CosmosDatabase cosmosDatabase) throws Exception {
		super(documentType);

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Load the attributes
		List<Property<?>> properties = new ArrayList<>();
		CabinetUtil.processFields(this.documentType, (context) -> {

			// Ignore key
			if (context.isKey()) {
				return;
			}

			// Ensure accessible
			Field field = context.getField();
			field.setAccessible(true);

			// Determine the property type
			Class<?> fieldClass = field.getType();
			PropertyType<?> propertyType = fieldTypeToPropertyType.get(fieldClass);
			if (propertyType == null) {
				// TODO load as embedded type
				throw new UnsupportedOperationException(
						"TODO implement embedded for " + field.getName() + " of type " + fieldClass.getName());
			}

			// Create and load the value
			Property<?> property = new Property<>(field, propertyType);
			properties.add(property);
		});
		this.properties = properties.toArray(Property[]::new);

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/id")));
		cosmosDatabase.createContainerIfNotExists(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
