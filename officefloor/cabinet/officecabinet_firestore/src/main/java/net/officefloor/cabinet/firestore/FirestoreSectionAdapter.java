package net.officefloor.cabinet.firestore;

import java.util.Map;

import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.spi.Index;

/**
 * {@link Firestore} {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSectionAdapter extends AbstractSectionAdapter<FirestoreSectionAdapter> {

	/**
	 * Creates the {@link FirestoreSectionMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param adapter      {@link FirestoreSectionAdapter}.
	 * @return {@link FirestoreSectionMetaData}.
	 * @throws Exception If fails to create {@link FirestoreSectionMetaData}.
	 */
	private <D> FirestoreSectionMetaData<D> createSectionMetaData(Class<D> documentType, Index[] indexes,
			FirestoreSectionAdapter adapter) throws Exception {
		return new FirestoreSectionMetaData<>(adapter, documentType);
	}

	/*
	 * ==================== AbstractSectionAdapter =========================
	 */

	@Override
	protected void initialise(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, FirestoreSectionAdapter>.Initialise init)
			throws Exception {

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createSectionMetaData);

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
