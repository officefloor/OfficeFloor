/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link TestSupport} to provide the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerTestSupport implements TestSupport {

	/**
	 * Sets up the capture of {@link OfficeManager}.
	 * 
	 * @param officeFloorBuilder {@link OfficeFloorBuilder}.
	 * @return {@link Supplier} to provide the captured {@link OfficeManager}.
	 */
	public static Supplier<OfficeManager> capture(OfficeFloorBuilder officeFloorBuilder) {
		CaptureOfficeManagerExecutive executive = new CaptureOfficeManagerExecutive();
		officeFloorBuilder.setExecutive(executive);
		return executive;
	}

	/**
	 * {@link CaptureOfficeManagerExecutive} instances per each
	 * {@link ProcessState}.
	 */
	private final List<Supplier<OfficeManager>> capturedOfficeManagers = new LinkedList<>();

	/**
	 * Obtains the {@link OfficeManager}.
	 * 
	 * @param processStateIndex Index of the created {@link ProcessState}.
	 * @return {@link OfficeManager}.
	 */
	public OfficeManager getOfficeManager(int processStateIndex) {
		assertTrue(processStateIndex < this.capturedOfficeManagers.size(),
				"No process yet started for " + processStateIndex);
		return this.capturedOfficeManagers.get(processStateIndex).get();
	}

	/*
	 * ===================== TestSupport =======================
	 */

	@Override
	public void init(ExtensionContext context) throws Exception {

		// Set up to capture the Office Manager
		ConstructTestSupport construct = TestSupportExtension.getTestSupport(ConstructTestSupport.class, context);

		// Set up to capture OfficeManager
		construct.addOfficeFloorEnhancer((officeFloorBuilder) -> {
			this.capturedOfficeManagers.add(capture(officeFloorBuilder));
		});
	}

	/**
	 * {@link Executive} to capture the {@link OfficeManager}.
	 */
	private static class CaptureOfficeManagerExecutive extends DefaultExecutive implements Supplier<OfficeManager> {

		/**
		 * Captured {@link OfficeManager}.
		 */
		private volatile OfficeManager officeManager;

		/*
		 * ======================= Executive ================================
		 */

		@Override
		public OfficeManager getOfficeManager(ProcessIdentifier processIdentifier, OfficeManager defaultOfficeManager) {
			this.officeManager = defaultOfficeManager;
			return defaultOfficeManager;
		}

		/*
		 * ========================= Supplier ===============================
		 */

		@Override
		public OfficeManager get() {
			assertNotNull(this.officeManager, "Need to run process to capture " + OfficeManager.class.getSimpleName());
			return this.officeManager;
		}
	}

}
