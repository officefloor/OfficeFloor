package net.officefloor;

import java.io.PrintStream;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Runs the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRunner {

	/**
	 * Compiles and opens the {@link OfficeFloor} and blocks until
	 * {@link OfficeFloor} is closed.
	 * 
	 * @param out  Std out.
	 * @param err  Std err.
	 * @param args Command line arguments.
	 * @throws Exception If fails to compile and open.
	 */
	public void runAndBlockToClose(PrintStream out, PrintStream err, String... args) throws Exception {

		// Determine whether opened
		MainOfficeFloorListener exitOnClose = new MainOfficeFloorListener();

		// Compile and open OfficeFloor
		this.compileAndOpen(exitOnClose, out, err, args);

		// Wait until closed
		exitOnClose.waitForClose();
		out.println("\nOfficeFloor closed");
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @param args Command line arguments.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open.
	 */
	public OfficeFloor run(String... args) throws Exception {
		return this.compileAndOpen(null, null, null, args);
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @param exitOnClose {@link MainOfficeFloorListener}.
	 * @param out         Std out.
	 * @param err         Std err.
	 * @param args        Command line arguments.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open.
	 */
	private OfficeFloor compileAndOpen(MainOfficeFloorListener exitOnClose, PrintStream out, PrintStream err,
			String... args) throws Exception {

		// Compile and open
		boolean isOpened = false;
		try {

			// Create the compiler
			OfficeFloorCompiler compiler = this.createCompiler();

			// Load the arguments as properties
			for (int i = 0; i < args.length; i += 2) {
				String name = args[i];
				String value = args[i + 1];
				compiler.addProperty(name, value);
			}

			// Determine if blocking, so enable stopping
			if (exitOnClose != null) {

				// Register the MBeans
				// (only means to gracefully close OfficeFloor, as will block thread waiting)
				compiler.setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());

				// Handle listening on close of OfficeFloor
				compiler.addOfficeFloorListener(exitOnClose);
			}

			// Compile the OfficeFloor
			if (out != null) {
				out.println("Compiling OfficeFloor");
			}
			OfficeFloor officeFloor = this.compile(compiler);

			// Open the OfficeFloor
			if (out != null) {
				out.println("Opening OfficeFloor");
			}
			this.open(officeFloor);
			if (out != null) {
				out.println("\n" + OfficeFloorMain.STD_OUT_RUNNING_LINE);
			}

			// Flag that opened
			isOpened = true;

			// Return the OfficeFloor
			return officeFloor;

		} finally {
			if ((!isOpened) && (err != null)) {
				// Indicate failed to open
				err.println("\n" + OfficeFloorMain.STD_ERR_FAIL_LINE);
			}
		}
	}

	/**
	 * Creates the {@link OfficeFloorCompiler}.
	 * 
	 * @return New {@link OfficeFloorCompiler}.
	 * @throws Exception If fails to create.
	 */
	protected OfficeFloorCompiler createCompiler() throws Exception {
		return OfficeFloorCompiler.newOfficeFloorCompiler(null);
	}

	/**
	 * Undertakes compiling the {@link OfficeFloor}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails compiling.
	 */
	protected OfficeFloor compile(OfficeFloorCompiler compiler) throws Exception {
		return compiler.compile("OfficeFloor");
	}

	/**
	 * Undertakes opening the {@link OfficeFloor}.
	 * 
	 * @param officeFloor {@link OfficeFloor}.
	 * @throws Exception If fails opening.
	 */
	protected void open(OfficeFloor officeFloor) throws Exception {
		officeFloor.openOfficeFloor();
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
		 * @throws InterruptedException If interrupted.
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
