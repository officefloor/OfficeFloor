package net.officefloor.cabinet.cosmosdb;

import java.util.Map;
import java.util.function.Function;

import com.azure.cosmos.implementation.InternalObjectNode;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * Cosmos DB {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDocumentAdapter extends AbstractDocumentAdapter<InternalObjectNode, InternalObjectNode> {

	/**
	 * Convenience to obtain value from {@link InternalObjectNode}.
	 * 
	 * @param <V>       Return type.
	 * @param transform Transforms retrieved {@link Integer} into value.
	 * @return {@link ScalarFieldValueGetter}.
	 */
	public static <V> ScalarFieldValueGetter<InternalObjectNode, V> getter(Function<Integer, V> transform) {
		return (doc, property) -> {
			Integer value = doc.getInt(property);
			return value != null ? transform.apply(value) : null;
		};
	}

	/**
	 * Convenience to set value on {@link InternalObjectNode}.
	 * 
	 * @param <P> Value to set on {@link InternalObjectNode}.
	 * @return {@link FieldValueSetter}.
	 */
	public static <P> FieldValueSetter<InternalObjectNode, P> setter() {
		return (doc, property, value, change) -> doc.set(property, value);
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public CosmosDocumentAdapter(AbstractOfficeStore<CosmosDocumentMetaData<?>> officeStore) {
		super(officeStore);
	}

	/*
	 * =================== AbstractOfficeCabinetAdapter ===================
	 */

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new InternalObjectNode());

		// Keys
		init.setKeyGetter((internalObjectNode, keyName) -> internalObjectNode.getId());
		init.setKeySetter((internalObjectNode, keyName, keyValue) -> internalObjectNode.setId(keyValue));

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, InternalObjectNode::getBoolean, translator(), setter(),
				serialiser(), deserialiser(Boolean::parseBoolean));
		init.addFieldType(byte.class, Byte.class, getter(Integer::byteValue), translator(Integer::valueOf), setter(),
				serialiser(), deserialiser(Byte::parseByte));
		init.addFieldType(short.class, Short.class, getter(Integer::shortValue), translator(Integer::valueOf), setter(),
				serialiser(), deserialiser(Short::parseShort));
		init.addFieldType(char.class, Character.class, (item, attributeName) -> {
			String value = item.getString(attributeName);
			return value != null ? value.charAt(0) : null;
		}, translator((value) -> new String(new char[] { value })), setter(), serialiser(), charDeserialiser());
		init.addFieldType(int.class, Integer.class, InternalObjectNode::getInt, translator(), setter(), serialiser(),
				deserialiser(Integer::parseInt));
		init.addFieldType(long.class, Long.class, (doc, property) -> {
			String value = doc.getString(property);
			return value != null ? Long.parseLong(value) : null;
		}, translator(String::valueOf), setter(), serialiser(), deserialiser(Long::parseLong));
		init.addFieldType(float.class, Float.class, (doc, property) -> {
			Double value = doc.getDouble(property);
			return value != null ? value.floatValue() : null;
		}, translator(Double::valueOf), setter(), serialiser(), deserialiser(Float::parseFloat));
		init.addFieldType(double.class, Double.class, InternalObjectNode::getDouble, translator(), setter(),
				serialiser(), deserialiser(Double::parseDouble));

		// Open types
		init.addFieldType(String.class, InternalObjectNode::getString, translator(), setter(), serialiser(),
				deserialiser((text) -> text));

		// Section type
		init.addFieldType(Map.class, (doc, property) -> (Map<String, Object>) doc.getMap(property), translator(),
				setter(), notSerialiseable(), notDeserialiseable(Map.class));
	}

}
