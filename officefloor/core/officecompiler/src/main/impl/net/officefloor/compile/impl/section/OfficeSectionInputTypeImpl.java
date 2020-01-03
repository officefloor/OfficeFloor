package net.officefloor.compile.impl.section;

import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * {@link OfficeAvailableSectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInputTypeImpl implements OfficeSectionInputType {

	/**
	 * Name of the {@link OfficeSectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link OfficeSectionInput}.
	 */
	private final String parameterType;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link OfficeSectionInput}.
	 * @param annotations
	 *            Annotations.
	 */
	public OfficeSectionInputTypeImpl(String inputName, String parameterType, Object[] annotations) {
		this.inputName = inputName;
		this.parameterType = parameterType;
		this.annotations = annotations;
	}

	/*
	 * ====================== OfficeSectionInputType ======================
	 */

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations;
	}

}