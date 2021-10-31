package net.officefloor.cabinet.domain.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.domain.CabinetSession;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * {@link CabinetSession} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetSessionImpl implements CabinetSession {

	/**
	 * Mapping of {@link Document} type to {@link OfficeCabinetArchive}.
	 */
	private final Map<Class<?>, ? extends OfficeCabinetArchive<?>> archives;

	/**
	 * {@link OfficeCabinet} instances for the session by their {@link Document}
	 * type.
	 */
	private final Map<Class<?>, OfficeCabinet<?>> cabinets = new ConcurrentHashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param archives Mapping of {@link Document} type to
	 *                 {@link OfficeCabinetArchive}.
	 */
	public CabinetSessionImpl(Map<Class<?>, ? extends OfficeCabinetArchive<?>> archives) {
		this.archives = archives;
	}

	/*
	 * =================== CabinetSession ============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <D> OfficeCabinet<D> getOfficeCabinet(Class<D> documentType) {
		return (OfficeCabinet<D>) this.cabinets.computeIfAbsent(documentType,
				(type) -> this.archives.get(type).createOfficeCabinet());
	}

	@Override
	public void close() throws Exception {
		for (OfficeCabinet<?> cabinet : this.cabinets.values()) {
			OfficeCabinetAdmin admin = (OfficeCabinetAdmin) cabinet;
			admin.close();
		}
	}

}
