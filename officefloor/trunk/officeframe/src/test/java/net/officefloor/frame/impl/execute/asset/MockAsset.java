/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.asset;

import junit.framework.TestCase;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.CheckAssetContext;

/**
 * Mock {@link Asset}.
 * 
 * @author Daniel
 */
public class MockAsset implements Asset {

	/**
	 * Failure of this {@link Asset}.
	 */
	protected Throwable failure = null;

	/**
	 * Specifies the failure for this {@link Asset}.
	 * 
	 * @param failure
	 *            Failure.
	 */
	public void setFailure(Throwable failure) {
		this.failure = failure;
	}

	/*
	 * ================= Asset ============================================
	 */

	@Override
	public void checkOnAsset(CheckAssetContext context) {
		TestCase
				.fail("TODO checkOnAsset functionality changed and tests require changing");
	}

}