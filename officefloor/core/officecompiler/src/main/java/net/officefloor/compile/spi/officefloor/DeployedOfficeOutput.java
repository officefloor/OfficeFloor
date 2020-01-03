package net.officefloor.compile.spi.officefloor;

/**
 * Output from a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeployedOfficeOutput {

	/**
	 * Obtains the name of the {@link DeployedOfficeOutput}.
	 * 
	 * @return Name of the {@link DeployedOfficeOutput}.
	 */
	String getDeployedOfficeOutputName();

}