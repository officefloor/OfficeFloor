package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.section.SectionObject;

/**
 * {@link SectionObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionObjectTypeImpl implements SectionObjectType {

	/**
	 * Name of the {@link SectionObject}.
	 */
	private final String objectName;

	/**
	 * Type of the {@link SectionObject}.
	 */
	private final String objectType;

	/**
	 * Type qualifier of the {@link SectionObject}.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 * @param objectType
	 *            Type of the {@link SectionObject}.
	 * @param typeQualifier
	 *            Type qualifier of the {@link SectionObject}.
	 */
	public SectionObjectTypeImpl(String objectName, String objectType,
			String typeQualifier) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ===================== SectionObjectType ============================
	 */

	@Override
	public String getSectionObjectName() {
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