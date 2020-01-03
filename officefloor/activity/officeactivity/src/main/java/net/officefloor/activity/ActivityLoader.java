package net.officefloor.activity;

/**
 * Loads the Activity configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityLoader {

	/**
	 * Loads the Activity configuration.
	 * 
	 * @param context {@link ActivityContext}.
	 * @throws Exception If fails to load the configuration.
	 */
	void loadActivityConfiguration(ActivityContext context) throws Exception;

}
