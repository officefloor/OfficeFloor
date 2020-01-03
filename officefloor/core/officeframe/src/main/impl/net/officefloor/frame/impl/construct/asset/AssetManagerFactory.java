package net.officefloor.frame.impl.construct.asset;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link AssetManagerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerFactory {

	/**
	 * Registry of the {@link AssetManager} instances by their name.
	 */
	private final Map<String, AssetManager> registry = new HashMap<String, AssetManager>();

	/**
	 * {@link ProcessState} for managing the {@link Office} where all mutations of
	 * the {@link Office} are undertaken on its main {@link ThreadState}.
	 */
	private final ProcessState officeManagerProcessState;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * Flag to ensure no further {@link AssetManager} instances are created once the
	 * {@link AssetManager} array is created.
	 */
	private boolean isAssetManagerListCreated = false;

	/**
	 * Instantiate.
	 * 
	 * @param officeManagerProcessState {@link ProcessState} for managing the
	 *                                  {@link Office} where all mutations of the
	 *                                  {@link Office} are undertaken on its main
	 *                                  {@link ThreadState}.
	 * @param monitorClock              {@link MonitorClock}.
	 * @param functionLoop              {@link FunctionLoop}.
	 */
	public AssetManagerFactory(ProcessState officeManagerProcessState, MonitorClock monitorClock,
			FunctionLoop functionLoop) {
		this.officeManagerProcessState = officeManagerProcessState;
		this.monitorClock = monitorClock;
		this.functionLoop = functionLoop;
	}

	/**
	 * Obtain all the registered {@link AssetManager} instances.
	 * 
	 * @return Registered {@link AssetManager} instances.
	 */
	public AssetManager[] getAssetManagers() {
		this.isAssetManagerListCreated = true;
		return this.registry.values().stream().toArray(AssetManager[]::new);
	}

	/**
	 * Creates the {@link AssetManager}.
	 * 
	 * @param assetType      {@link AssetType}.
	 * @param assetName      Name of the {@link Asset}.
	 * @param responsibility Responsibility of the {@link AssetManager} for the
	 *                       {@link Asset}.
	 * @param issues         {@link OfficeFloorIssues}.
	 * @return {@link AssetManager} or <code>null</code> if {@link AssetManager}
	 *         already created for the {@link Asset} with {@link OfficeFloorIssues}
	 *         informed.
	 */
	public AssetManager createAssetManager(AssetType assetType, String assetName, String responsibility,
			OfficeFloorIssues issues) {

		// Create the name of the asset manager
		String assetManagerName = assetType.toString() + ":" + assetName + "[" + responsibility + "]";

		// Ensure will be included in the list
		if (this.isAssetManagerListCreated) {
			throw new IllegalStateException("Can not create " + AssetManager.class.getSimpleName() + " "
					+ assetManagerName + " as list already generated");
		}

		// Determine if manager already available for responsibility over asset
		if (this.registry.get(assetManagerName) != null) {
			issues.addIssue(assetType, assetName,
					AssetManager.class.getSimpleName() + " already responsible for '" + responsibility + "'");
			return null; // can not carry on
		}

		// Create the asset manager
		AssetManager assetManager = new AssetManagerImpl(this.officeManagerProcessState, this.monitorClock,
				this.functionLoop);

		// Register the asset manager
		this.registry.put(assetManagerName, assetManager);

		// Return the asset manager
		return assetManager;
	}

}