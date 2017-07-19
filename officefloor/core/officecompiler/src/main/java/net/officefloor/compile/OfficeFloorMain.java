/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile;

import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides <code>main</code> method to compile and run {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMain {

	/**
	 * Compiles and run {@link OfficeFloor}.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to compile and open.
	 */
	public static void main(String[] args) throws Exception {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Register the MBeans
		// (only means to gracefully close OfficeFloor, without killing process)
		compiler.setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());

		// Handle listening on close of OfficeFloor
		MainOfficeFloorListener exitOnClose = new MainOfficeFloorListener();
		compiler.addOfficeFloorListener(exitOnClose);

		// Compile the OfficeFloor
		System.out.println("Compiling OfficeFloor");
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");

		// Open the OfficeFloor
		System.out.println("Opening OfficeFloor");
		officeFloor.openOfficeFloor();
		System.out.println("OfficeFloor running");

		// Wait until closed
		exitOnClose.waitForClose();
		System.out.println("OfficeFloor closed");
	}

	/**
	 * {@link OfficeFloorListener} for main method.
	 */
	private static class MainOfficeFloorListener implements OfficeFloorListener {

		/**
		 * Flag indicating if closed.
		 */
		private boolean isClosed = false;

		/**
		 * Waits until the {@link OfficeFloor} is closed.
		 * 
		 * @throws InterruptedException
		 *             If interrupted.
		 */
		private synchronized void waitForClose() throws InterruptedException {

			// Do not wait if closed
			if (this.isClosed) {
				return;
			}

			// Wait until closed
			this.wait();
		}

		/*
		 * ==================== OfficeFloorListener =======================
		 */

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
			// Do nothing
		}

		@Override
		public synchronized void officeFloorClosed(OfficeFloorEvent event) throws Exception {

			// Flag closed
			this.isClosed = true;

			// Notify that closed
			this.notify();
		}
	}

}