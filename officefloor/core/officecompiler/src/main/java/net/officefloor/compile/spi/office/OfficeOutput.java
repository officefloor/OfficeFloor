package net.officefloor.compile.spi.office;

/**
 * Output from the {@link OfficeOutput}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutput extends OfficeFlowSinkNode {

	/**
	 * Obtains the name of this {@link OfficeOutput}.
	 * 
	 * @return Name of this {@link OfficeOutput}.
	 */
	String getOfficeOutputName();

}