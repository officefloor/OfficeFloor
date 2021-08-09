/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		return CaptureOfficeManagerExecutive.capture(officeFloorBuilder);
	}

	/**
	 * {@link CaptureOfficeManagerExecutive} instances per each
	 * {@link ProcessState}.
	 */
	private final List<CaptureOfficeManagerExecutive> capturedOfficeManagers = new LinkedList<>();

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

	/**
	 * Runs asset check on all captured {@link OfficeManager} instances.
	 */
	public void runAssetChecks() {
		for (CaptureOfficeManagerExecutive capture : this.capturedOfficeManagers) {
			if (capture.officeManager != null) {
				capture.officeManager.runAssetChecks();
			}
		}
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
			this.capturedOfficeManagers.add(CaptureOfficeManagerExecutive.capture(officeFloorBuilder));
		});
	}

	/**
	 * {@link Executive} to capture the {@link OfficeManager}.
	 */
	private static class CaptureOfficeManagerExecutive extends DefaultExecutive implements Supplier<OfficeManager> {

		/**
		 * Sets up the capture of {@link OfficeManager}.
		 * 
		 * @param officeFloorBuilder {@link OfficeFloorBuilder}.
		 * @return {@link CaptureOfficeManagerExecutive}.
		 */
		private static CaptureOfficeManagerExecutive capture(OfficeFloorBuilder officeFloorBuilder) {
			CaptureOfficeManagerExecutive executive = new CaptureOfficeManagerExecutive();
			officeFloorBuilder.setExecutive(executive);
			return executive;
		}

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
