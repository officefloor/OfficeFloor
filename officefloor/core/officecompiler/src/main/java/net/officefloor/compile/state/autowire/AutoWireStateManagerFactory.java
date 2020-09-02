package net.officefloor.compile.state.autowire;

/**
 * Creates an {@link AutoWireStateManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireStateManagerFactory {

	/**
	 * Creates the {@link AutoWireStateManager}.
	 * 
	 * @return {@link AutoWireStateManager}.
	 */
	AutoWireStateManager createAutoWireStateManager();

}