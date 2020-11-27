package net.officefloor.frame.internal.structure;

/**
 * Reference to an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetManagerReference {

	/**
	 * Obtains the index of the {@link AssetManager} within the
	 * {@link OfficeManager}.
	 * 
	 * @return Index of the {@link AssetManager} within the {@link OfficeManager}.
	 */
	int getAssetManagerIndex();
}