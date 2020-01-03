package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * {@link OfficeInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputTypeImpl implements OfficeInputType {

	/**
	 * Name of the {@link OfficeInput}.
	 */
	private final String inputName;

	/**
	 * {@link Class} name of the parameter.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeInput}.
	 * @param parameterType
	 *            {@link Class} name of the parameter.
	 */
	public OfficeInputTypeImpl(String inputName, String parameterType) {
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * ================== OfficeInputType ======================
	 */

	@Override
	public String getOfficeInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

}