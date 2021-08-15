package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;

/**
 * Abstract {@link OfficeCabinet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * Instances used within the {@link OfficeCabinet} session.
	 */
	private final Map<String, D> session = new HashMap<>();

	/**
	 * Retrieves the {@link Document} by the key.
	 * 
	 * @param key Key for the {@link Document}.
	 * @return {@link Document} or <code>null</code> if not exists.
	 */
	protected abstract D _retrieveByKey(String key);

	/**
	 * Stores the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} to the stored {@link Document}.
	 */
	protected abstract String _store(D document);

	/*
	 * ==================== OfficeCabinet ========================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Determine if have in session
		D document = this.session.get(key);
		if (document == null) {

			// Not in session, so attempt to retrieve
			document = this._retrieveByKey(key);
			if (document != null) {

				// Capture in session
				this.session.put(key, document);
			}
		}

		// Return the document
		return document != null ? Optional.of(document) : Optional.empty();
	}

	@Override
	public void store(D document) {

		// Store the changes
		String key = this._store(document);

		// Update session with document
		this.session.put(key, document);
	}

}