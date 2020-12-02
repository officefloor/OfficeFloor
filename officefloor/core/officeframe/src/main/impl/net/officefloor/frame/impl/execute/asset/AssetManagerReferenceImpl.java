package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerReference;

/**
 * {@link AssetManagerReference} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerReferenceImpl implements AssetManagerReference {

	/**
	 * Index of the {@link AssetManager}.
	 */
	private final int assetManagerIndex;

	/**
	 * Instantiate.
	 * 
	 * @param assetManagerIndex Index of the {@link AssetManager}.
	 */
	public AssetManagerReferenceImpl(int assetManagerIndex) {
		this.assetManagerIndex = assetManagerIndex;
	}

	/*
	 * ===================== AssetManagerReference =====================
	 */

	@Override
	public int getAssetManagerIndex() {
		return this.assetManagerIndex;
	}

}
