package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Manages the {@link AssetManager} instances within an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManager {

	/**
	 * Starts this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 */
	void startManaging();

	/**
	 * Runs the checks on the {@link Asset} instances within the {@link Office}.
	 */
	void runAssetChecks();

	/**
	 * Stops this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 */
	void stopManaging();

}