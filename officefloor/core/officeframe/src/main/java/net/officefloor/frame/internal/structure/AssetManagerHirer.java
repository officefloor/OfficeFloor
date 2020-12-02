package net.officefloor.frame.internal.structure;

/**
 * Hires an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetManagerHirer {

	/**
	 * Hires an {@link AssetManager} running under the {@link ProcessState}.
	 * 
	 * @param managingProcessState Managing {@link ProcessState} for the
	 *                             {@link AssetManager}.
	 * @return Newly hired {@link AssetManager}.
	 */
	AssetManager hireAssetManager(ProcessState managingProcessState);

}