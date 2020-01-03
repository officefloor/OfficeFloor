package net.officefloor.frame.impl;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.impl.construct.officefloor.OfficeFloorBuilderImpl;

/**
 * Default implementation of the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFrameImpl extends OfficeFrame {

	/*
	 * ======================== OfficeFrame ==================================
	 */

	@Override
	public OfficeFloorBuilder createOfficeFloorBuilder(String officeFloorName) {
		return new OfficeFloorBuilderImpl(officeFloorName);
	}

}