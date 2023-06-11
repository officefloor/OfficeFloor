package net.officefloor.cabinet.cosmosdb;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;

/**
 * Cosmos DB {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosSectionAdapter extends AbstractSectionAdapter {

	/**
	 * Instantiate.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public CosmosSectionAdapter(AbstractOfficeStore officeStore) {
		super(officeStore);
	}

	/*
	 * ================== AbstractSectionAdapter =======================
	 */

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Primitives overrides (only boolean, int and double supported)
		init.addFieldType(byte.class, Byte.class, getter(Integer::byteValue), translator(Byte::intValue), setter(),
				serialiser(), deserialiser(Byte::parseByte));
		init.addFieldType(short.class, Short.class, getter(Integer::shortValue), translator(Short::intValue), setter(),
				serialiser(), deserialiser(Short::parseShort));
		init.addFieldType(long.class, Long.class, getter((mapValue) -> Long.parseLong((String) mapValue)),
				translator(String::valueOf), setter(), serialiser(), deserialiser(Long::parseLong));
		init.addFieldType(float.class, Float.class, getter(Number::floatValue), translator(Number::doubleValue),
				setter(), serialiser(), deserialiser(Float::parseFloat));
		init.addFieldType(double.class, Double.class, getter(Number::doubleValue), translator(Number::doubleValue),
				setter(), serialiser(), deserialiser(Double::parseDouble));
	}

}