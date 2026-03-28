package net.officefloor.server.google.function.maven;

import com.google.cloud.firestore.Firestore;

import net.officefloor.OfficeFloorRunner;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.OfficeFloorHttpFunction.HttpFunctionOfficeFloorFactory;

/**
 * Main runner for {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionMain extends OfficeFloorRunner implements HttpFunctionOfficeFloorFactory {

	/**
	 * {@link Property} name for the {@link Firestore} port.
	 */
	public static final String FIRESTORE_PORT_NAME = "officefloor.google.function.firestore.port";

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 */
	@FunctionalInterface
	private static interface MainOfficeFloorFactory {

		/**
		 * Compiles and opens the {@link OfficeFloor}.
		 * 
		 * @return {@link OfficeFloor}.
		 * @throws Exception If fails to compile and open.
		 */
		OfficeFloor compileAndOpenOfficeFloor() throws Exception;
	}

	/**
	 * Main function.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails.
	 */
	public static void main(String... args) throws Exception {
		OfficeFloorHttpFunctionMain main = new OfficeFloorHttpFunctionMain();
		main.runWithHttpFunctionSetup(() -> {
			main.runAndBlockToClose(System.out, System.err, args);
			return null; // closed plus ignored
		});
	}

	/**
	 * {@link MavenFirestore}.
	 */
	private MavenFirestore firestore;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @param args Command line arguments.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open.
	 */
	public OfficeFloor open(String... args) throws Exception {
		return this.runWithHttpFunctionSetup(() -> this.run(args));
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @param factory {@link MainOfficeFloorFactory}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open.
	 */
	private OfficeFloor runWithHttpFunctionSetup(MainOfficeFloorFactory factory) throws Exception {
		OfficeFloorHttpFunction.setOfficeFloorFactory(this);
		return factory.compileAndOpenOfficeFloor();
	}

	/**
	 * Setup logic.
	 */
	@FunctionalInterface
	private static interface SetupLogic<R> {

		/**
		 * Set up logic.
		 * 
		 * @return Possible value.
		 * @throws Exception If fails.
		 */
		R setup() throws Exception;
	}

	/**
	 * Manages {@link Firestore}.
	 * 
	 * @param logic {@link SetupLogic}.
	 * @return Possible setup value.
	 * @throws Exception If fails setup.
	 */
	private <R> R manageFirestore(SetupLogic<R> logic) throws Exception {
		boolean isComplete = false;
		try {

			// Undertake setup
			R result = logic.setup();

			// Flag complete
			isComplete = true;

			// Return the result
			return result;

		} finally {
			if (!isComplete) {
				try {
					// Not complete, so stop Firestore immediately
					this.firestore.stopFirestore();
				} catch (Exception ex) {
					// Avoid propagating possible firestore failure
					ex.printStackTrace();
				}
			}
		}
	}

	/*
	 * ===================== OfficeFloorRunner ==========================
	 */

	@Override
	protected OfficeFloor compile(OfficeFloorCompiler compiler) throws Exception {

		// Start firestore
		String firestorePort = System.getProperty(FIRESTORE_PORT_NAME);
		this.firestore = new MavenFirestore(Integer.parseInt(firestorePort));
		this.firestore.startFirestore();
		
		// Ensure firestore running
		this.firestore.getFirestore();
		
		// Ensure clean up firestore immediately on failure
		return this.manageFirestore(() -> {

			// Provide clean up on close of OfficeFloor
			compiler.addOfficeFloorListener(new OfficeFloorListener() {

				@Override
				public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
					// Do nothing
				}

				@Override
				public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
					try {
						// Reset
						OfficeFloorHttpFunction.reset();
					} finally {
						// Stop Firestore
						OfficeFloorHttpFunctionMain.this.firestore.stopFirestore();
					}
				}
			});

			// Compile the OfficeFloor
			return super.compile(compiler);
		});
	}

	@Override
	protected void open(OfficeFloor officeFloor) throws Exception {
		this.manageFirestore(() -> {
		
			// Open the OfficeFloor
			this.officeFloor = officeFloor;
			super.open(officeFloor);

			// Ensure load the instance (as must be on same thread)
			OfficeFloorHttpFunction.open();

			// Nothing to return
			return null;
		});
	}

	/*
	 * ================ HttpFunctionOfficeFloorFactory ==================
	 */

	@Override
	public OfficeFloor compileAndOpenOfficeFloor() throws Exception {
		return this.officeFloor;
	}

}