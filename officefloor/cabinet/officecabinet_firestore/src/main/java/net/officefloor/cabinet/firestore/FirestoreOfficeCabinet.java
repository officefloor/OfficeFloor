package net.officefloor.cabinet.firestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.firestore.FirestoreOfficeCabinetMetaData.MapValue;

/**
 * {@link Firestore} {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * {@link FirestoreOfficeCabinetMetaData}.
	 */
	private final FirestoreOfficeCabinetMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link FirestoreOfficeCabinetMetaData}.
	 */
	public FirestoreOfficeCabinet(FirestoreOfficeCabinetMetaData<D> metaData) {
		this.metaData = metaData;
	}

	/*
	 * =================== OfficeCabinet =======================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Retrieve the document
		DocumentReference docRef = this.metaData.firestore.collection(this.metaData.collectionId).document(key);
		try {

			// Obtain the document
			DocumentSnapshot snapshot = docRef.get().get();

			// Create the document
			D document = this.metaData.documentType.getConstructor().newInstance();

			// Load the attributes
			for (MapValue<?, ?> mapValue : this.metaData.mapValues) {

				// Obtain the value
				String fieldName = mapValue.field.getName();
				Object fieldValue = mapValue.mapValueType.fromSnapshot.fromSnapshot(snapshot, fieldName);

				// Load value to document
				mapValue.field.set(document, fieldValue);
			}

			// Return the document
			return Optional.of(document);

		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to obtain document " + this.metaData.documentType.getName() + " by key " + key, ex);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void store(D document) {

		// Obtain key and determine if new
		String key;
		boolean isNew = false;
		try {
			// Obtain the key for the document
			key = this.metaData.documentKey.getKey(document);
			if (key == null) {

				// Generate and load key
				key = CabinetUtil.newKey();
				this.metaData.documentKey.setKey(document, key);

				// Flag creating
				isNew = true;
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Create the fields to store
		Map<String, Object> fields = new HashMap<>();
		try {
			for (MapValue mapValue : this.metaData.mapValues) {

				// Obtain the name
				String fieldName = mapValue.field.getName();

				// Obtain the value
				Object fieldValue = mapValue.field.get(document);
				Object value = mapValue.mapValueType.toMap.toMap(fieldValue);

				// Load value
				fields.put(fieldName, value);
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed extracting data from document " + this.metaData.documentType.getName() + " by key " + key,
					ex);
		}

		// Save document
		try {
			DocumentReference docRef = this.metaData.firestore.collection(this.metaData.collectionId).document(key);
			if (isNew) {
				docRef.create(fields).get();
			} else {
				docRef.set(fields).get();
			}
		} catch (ExecutionException | InterruptedException ex) {
			throw new IllegalStateException(
					"Failed to store document " + this.metaData.documentType.getName() + " by key " + key, ex);
		}
	}

}