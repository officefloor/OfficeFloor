package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link AssetManagerHirer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerHirerImpl implements AssetManagerHirer {

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock clock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop loop;

	/**
	 * Instantiate.
	 * 
	 * @param clock {@link MonitorClock}.
	 * @param loop  {@link FunctionLoop}.
	 */
	public AssetManagerHirerImpl(MonitorClock clock, FunctionLoop loop) {
		this.clock = clock;
		this.loop = loop;
	}

	/*
	 * ===================== AssetManagerHirer ========================
	 */

	@Override
	public AssetManager hireAssetManager(ProcessState managingProcess) {
		return new AssetManagerImpl(managingProcess, this.clock, this.loop);
	}

}
