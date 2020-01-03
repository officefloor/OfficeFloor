package net.officefloor.frame.internal.structure;

/**
 * {@link AssetManager} to manage {@link Asset} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetManager extends FunctionState {

	/**
	 * <p>
	 * Creates a new {@link AssetLatch}.
	 * <p>
	 * The returned {@link AssetLatch} is not being managed by this
	 * {@link AssetManager}. To have the {@link AssetLatch} managed, it must be
	 * registered with this {@link AssetManager}. This allows for only the list
	 * of {@link AssetLatch} instances requiring management to be managed.
	 * 
	 * @param asset
	 *            {@link Asset} that {@link FunctionState} instances will wait on.
	 * @return {@link AssetLatch} for the {@link Asset}.
	 */
	AssetLatch createAssetLatch(Asset asset);

}