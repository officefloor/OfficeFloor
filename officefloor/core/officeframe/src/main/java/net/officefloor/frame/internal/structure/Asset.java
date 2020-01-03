package net.officefloor.frame.internal.structure;

/**
 * {@link Asset} of an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Asset {

	/**
	 * Obtains the {@link ThreadState} owning this {@link Asset}.
	 * 
	 * @return {@link ThreadState} owning this {@link Asset}.
	 */
	ThreadState getOwningThreadState();

	/**
	 * Checks on the {@link Asset}.
	 * 
	 * @param context
	 *            {@link CheckAssetContext} for checking on the {@link Asset}.
	 */
	void checkOnAsset(CheckAssetContext context);

}