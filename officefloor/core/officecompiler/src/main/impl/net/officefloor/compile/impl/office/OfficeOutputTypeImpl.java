package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.spi.office.OfficeOutput;

/**
 * {@link OfficeOutputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeOutputTypeImpl implements OfficeOutputType {

	/**
	 * Name of the {@link OfficeOutput}.
	 */
	private final String outputName;

	/**
	 * Argument type of the {@link OfficeOutput}.
	 */
	private final String argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeOutput}.
	 * @param argumentType
	 *            Argument type of the {@link OfficeOutput}.
	 */
	public OfficeOutputTypeImpl(String outputName, String argumentType) {
		this.outputName = outputName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== OfficeOutputType =========================
	 */

	@Override
	public String getOfficeOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

}