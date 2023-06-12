package net.officefloor.cabinet.firestore;

import java.util.Map;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;

/**
 * {@link Firestore} {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSectionAdapter extends AbstractSectionAdapter {

	/**
	 * Instantiate.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public FirestoreSectionAdapter(AbstractOfficeStore officeStore) {
		super(officeStore);
	}

	/*
	 * ==================== AbstractSectionAdapter =========================
	 */

	@Override
	protected void initialise(AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>>.Initialise init)
			throws Exception {

		// Primitive overrides
		init.addFieldType(byte.class, Byte.class, getter(Long::byteValue), translator(Byte::longValue), setter(),
				serialiser(), deserialiser(Byte::parseByte));
		init.addFieldType(short.class, Short.class, getter(Long::shortValue), translator(Short::longValue), setter(),
				serialiser(), deserialiser(Short::parseShort));
		init.addFieldType(int.class, Integer.class, getter(Long::intValue), translator(Integer::longValue), setter(),
				serialiser(), deserialiser(Integer::parseInt));
		init.addFieldType(float.class, Float.class, getter(Double::floatValue), translator(Float::doubleValue),
				setter(), serialiser(), deserialiser(Float::parseFloat));
	}

}
