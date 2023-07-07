package net.officefloor.server.google.function.wrap;

import java.util.LinkedList;
import java.util.List;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.officefloor.OfficeFloorHttpFunctionReference;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Abstract functionality for setting up using Google {@link HttpFunction}.
 */
public class AbstractSetupGoogleHttpFunctionJUnit<J extends AbstractSetupGoogleHttpFunctionJUnit<J>> {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	static final ThreadLocal<OfficeFloorExtensionService> officeFloorExtensionService = new ThreadLocal<>();

	/**
	 * {@link OfficeExtensionService}.
	 */
	static final ThreadLocal<OfficeExtensionService> officeExtensionService = new ThreadLocal<>();

	/**
	 * {@link HttpFunction} {@link Class}.
	 */
	private final Class<?> httpFunctionClass;

	/**
	 * {@link OfficeFloorExtensionService} instances.
	 */
	private final List<OfficeFloorExtensionService> officeFloorExtensions = new LinkedList<>();

	/**
	 * {@link OfficeExtensionService} instances.
	 */
	private final List<OfficeExtensionService> officeExtensions = new LinkedList<>();

	/**
	 * {@link OfficeFloor} instances.
	 */
	private final List<OfficeFloor> officeFloors = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public AbstractSetupGoogleHttpFunctionJUnit(Class<?> httpFunctionClass) {
		this.httpFunctionClass = httpFunctionClass;
	}

	/**
	 * Instantiate using default {@link OfficeFloor} {@link HttpFunction}.
	 */
	public AbstractSetupGoogleHttpFunctionJUnit() {
		this.httpFunctionClass = OfficeFloorHttpFunctionReference.getOfficeFloorHttpFunctionClass();
	}

	/**
	 * Adds an {@link OfficeFloorExtensionService}.
	 * 
	 * @param extension {@link OfficeFloorExtensionService}.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public J officeFloor(OfficeFloorExtensionService extension) {
		this.officeFloorExtensions.add(extension);
		return (J) this;
	}

	/**
	 * Adds an {@link OfficeExtensionService}.
	 * 
	 * @param extension {@link OfficeExtensionService}.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public J office(OfficeExtensionService extension) {
		this.officeExtensions.add(extension);
		return (J) this;
	}

	/**
	 * Sets up the Google {@link HttpFunction}.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService} to provide
	 *                             access to servicing {@link HttpFunction}.
	 */
	protected void setupHttpFunction(OfficeFloorExtensionService officeFloorExtension) {

		// Easy access to test case
		AbstractSetupGoogleHttpFunctionJUnit<J> testCase = AbstractSetupGoogleHttpFunctionJUnit.this;

		// Configure the access to HTTP Function
		officeFloorExtensionService.set(new OfficeFloorExtensionService() {

			@Override
			public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
					throws Exception {

				// Setup servicing
				officeFloorExtension.extendOfficeFloor(officeFloorDeployer, context);

				// Undertake additional extensions
				for (OfficeFloorExtensionService extension : testCase.officeFloorExtensions) {
					extension.extendOfficeFloor(officeFloorDeployer, context);
				}

				// Capture the OfficeFloor for test tear down
				officeFloorDeployer.addOfficeFloorListener(new SetupOfficeFloorListener());
			}
		});

		// Configure the servicing
		officeExtensionService.set(new OfficeExtensionService() {

			@Override
			public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

				// Auto-wire to connect objects
				officeArchitect.enableAutoWireObjects();

				// Configure HTTP Function handling
				officeArchitect.addOfficeSection(HttpFunctionSectionSource.SECTION_NAME,
						new HttpFunctionSectionSource(testCase.httpFunctionClass), null);

				// Undertake additional extensions
				for (OfficeExtensionService extension : testCase.officeExtensions) {
					extension.extendOffice(officeArchitect, context);
				}
			}
		});
	}

	/**
	 * Tears down the Google {@link HttpFunction}.
	 */
	protected void teardownHttpFunction() {

		// Clear extensions to no longer setup
		officeFloorExtensionService.remove();
		officeExtensionService.remove();

		// Ensure all open OfficeFloor instances are closed
		Throwable exception = null;
		for (OfficeFloor officeFloor : this.officeFloors) {
			try {
				officeFloor.close();
			} catch (Throwable ex) {
				// Propagate first failure
				if (exception == null) {
					exception = ex;
				} else {
					// Indicate multiple failures
					ex.printStackTrace();
				}
			}
		}
		if (exception != null) {
			JUnitAgnosticAssert.fail(exception);
		}
	}

	/**
	 * {@link OfficeFloorListener} to obtain access to {@link OfficeFloor} for clean
	 * up.
	 */
	private class SetupOfficeFloorListener implements OfficeFloorListener {

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
			AbstractSetupGoogleHttpFunctionJUnit.this.officeFloors.add(event.getOfficeFloor());
		}

		@Override
		public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
			// Do nothing
		}
	}

}