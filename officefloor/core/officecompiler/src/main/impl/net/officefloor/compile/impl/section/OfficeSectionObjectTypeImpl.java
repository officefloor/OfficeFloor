package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.spi.office.OfficeSectionObject;

/**
 * {@link OfficeSectionObjectTypeImpl} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionObjectTypeImpl implements OfficeSectionObjectType {

	/**
	 * Name of the {@link OfficeSectionObject}.
	 */
	private final String objectName;

	/**
	 * Type of the {@link OfficeSectionObject}.
	 */
	private final String objectType;

	/**
	 * Type qualifier for the {@link OfficeSectionObject}.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeSectionObject}.
	 * @param objectType
	 *            Type of the {@link OfficeSectionObject}.
	 * @param typeQualifier
	 *            Type qualifier for the {@link OfficeSectionObject}.
	 */
	public OfficeSectionObjectTypeImpl(String objectName, String objectType,
			String typeQualifier) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ================= OfficeSectionObjectType =====================
	 */

	@Override
	public String getOfficeSectionObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

}