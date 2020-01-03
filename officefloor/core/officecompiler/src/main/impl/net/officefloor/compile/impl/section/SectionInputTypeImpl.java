package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.section.SectionInput;

/**
 * {@link SectionInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionInputTypeImpl implements SectionInputType {

	/**
	 * Name of the {@link SectionInput}.
	 */
	private final String inputName;

	/**
	 * Parameter type of the {@link SectionInput}.
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
	 *            Name of the {@link SectionInput}.
	 * @param parameterType
	 *            Parameter type of the {@link SectionInput}.
	 * @param annotations
	 *            Annotations.
	 */
	public SectionInputTypeImpl(String inputName, String parameterType, Object[] annotations) {
		this.inputName = inputName;
		this.parameterType = parameterType;
		this.annotations = annotations;
	}

	/*
	 * ==================== SectionInputType =========================
	 */

	@Override
	public String getSectionInputName() {
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