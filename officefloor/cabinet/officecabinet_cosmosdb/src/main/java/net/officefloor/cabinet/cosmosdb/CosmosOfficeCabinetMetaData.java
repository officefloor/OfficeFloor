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
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.DocumentKey;

/**
 * Meta-data for the {@link CosmosOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
class CosmosOfficeCabinetMetaData<D> {

	/**
	 * Mapping of {@link Field} type to property type.
	 */
	private static final Map<Class<?>, PropertyType<?>> fieldTypeToPropertyType = new HashMap<>();

	private static <T> void addFielfdTypeToPropertyType(Class<T> fieldType, PropertyGetter<T> getter,
			PropertySetter<T> setter) {
		fieldTypeToPropertyType.put(fieldType, new PropertyType<>(getter, setter));
	}

	static {
		// Numbers
		addFielfdTypeToPropertyType(boolean.class, InternalObjectNode::getBoolean, InternalObjectNode::set);
		addFielfdTypeToPropertyType(byte.class, (doc, property) -> doc.getInt(property).byteValue(),
				(doc, property, value) -> doc.set(property, Integer.valueOf(value)));
		addFielfdTypeToPropertyType(short.class, (doc, property) -> doc.getInt(property).shortValue(),
				(doc, property, value) -> doc.set(property, Integer.valueOf(value)));
		addFielfdTypeToPropertyType(int.class, InternalObjectNode::getInt, InternalObjectNode::set);
		addFielfdTypeToPropertyType(long.class, (doc, property) -> Long.parseLong(doc.getString(property)),
				(doc, property, value) -> doc.set(property, String.valueOf(value)));
		addFielfdTypeToPropertyType(float.class, (doc, property) -> doc.getDouble(property).floatValue(),
				(doc, property, value) -> doc.set(property, Double.valueOf(value)));
		addFielfdTypeToPropertyType(double.class, InternalObjectNode::getDouble, InternalObjectNode::set);

		// Strings
		addFielfdTypeToPropertyType(char.class, (item, attributeName) -> item.getString(attributeName).charAt(0),
				(doc, property, value) -> doc.set(property, new String(new char[] { value })));
		addFielfdTypeToPropertyType(String.class, InternalObjectNode::getString, InternalObjectNode::set);
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
	 * {@link Document} type.
	 */
	final Class<D> documentType;

	/**
	 * {@link DocumentKey}.
	 */
	final DocumentKey<D> documentKey;

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
		this.documentType = documentType;

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Search out the key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);

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
		cosmosDatabase.createContainer(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

}
