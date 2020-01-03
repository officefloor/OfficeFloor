package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link OfficeAvailableSectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeAvailableSectionInputTypeImpl implements
		OfficeAvailableSectionInputType {

	/**
	 * Name of the {@link OfficeSection}.
	 */
	private final String sectionName;

	/**
	 * Name of the {@link OfficeSectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type for the {@link OfficeSectionInput}.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param parameterType
	 *            Parameter type for the {@link OfficeSectionInput}.
	 */
	public OfficeAvailableSectionInputTypeImpl(String sectionName,
			String inputName, String parameterType) {
		this.sectionName = sectionName;
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * =================== OfficeAvailableSectionInputType =============
	 */

	@Override
	public String getOfficeSectionName() {
		return this.sectionName;
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

}