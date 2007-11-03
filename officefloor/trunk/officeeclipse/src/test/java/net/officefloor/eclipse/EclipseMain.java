/**
 * 
 */
package net.officefloor.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * JUnit Plug-in Test that keeps the {@link IWorkbench} running.
 * <p>
 * To run:
 * <ol>
 * <li>Right click on this class</li>
 * <li>Select Run As -> JUnit Plug-in Test</li>
 * <li>Allow to start up and then exist</li>
 * <li>Click on Open Run Dialog... (under arrow of green play button)</li>
 * <li>Go to the Main tab of the just created test</li>
 * <li>Change Location to: ${workspace_loc}/../test-workspace</li>
 * <li>Select to clear Log only</li>
 * <li>May add to favourties now for running</li>
 * </ol>
 * 
 * @author Daniel
 */
public class EclipseMain extends TestCase {

	/**
	 * Invoked by the plugin unit test and will stop Eclipse from exiting.
	 */
	public void testMain() {

		System.out.println("testMain");
		
		// Add listener to stop shutdown
		PlatformUI.getWorkbench().addWorkbenchListener(
				new IWorkbenchListener() {

					/**
					 * Indicates if first shutdown (ie stops the unit test from
					 * finishing).
					 */
					private boolean isFirst = true;

					@Override
					public boolean preShutdown(IWorkbench arg0, boolean arg1) {

						// Stop shutdown on first unit test
						if (this.isFirst) {

							// Determine if wait for shutdown
							if ("yes".equalsIgnoreCase(System
									.getProperty("enter.stop"))) {
								System.out.print("Press enter to exit ");
								try {
									new BufferedReader(new InputStreamReader(
											System.in)).readLine();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}

							this.isFirst = false;
							return false;
						}

						// Allow for manual shutdown
						return true;
					}

					@Override
					public void postShutdown(IWorkbench arg0) {
						// Do nothing
					}
				});
	}
}
