/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.tool.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.officefloor.demo.gui.DemoTool;
import net.officefloor.demo.record.FrameVisibilityListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * {@link IWorkbenchWindowActionDelegate} for {@link DemoAction}.
 * 
 * @author Daniel Sagenschneider
 */
public class DemoAction implements IWorkbenchWindowActionDelegate {

	/**
	 * {@link IWorkbenchWindow}.
	 */
	private IWorkbenchWindow window;

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		try {

			// Obtain the active window location
			IWorkbench workbench = this.window.getWorkbench();
			final Display display = workbench.getDisplay();

			// Assume the active shell to be the Eclipse workbench
			Shell eclipseShell = display.getActiveShell();

			// Create the Frame for the Demo
			final Shell demoShell = new Shell(display);
			demoShell.setText("Demo Tool");
			demoShell.setLayout(new FillLayout());
			Composite composite = new Composite(demoShell, SWT.EMBEDDED
					| SWT.NO_BACKGROUND);
			composite.setLayout(new FillLayout());
			Frame frame = SWT_AWT.new_Frame(composite);
			frame.setLayout(new BorderLayout());

			// Add the root pane
			JPanel basePanel = new JPanel(new BorderLayout());
			JRootPane rootPane = new JRootPane();
			final Container pane = rootPane.getContentPane();
			pane.setLayout(new BorderLayout());
			basePanel.add(rootPane);
			frame.add(basePanel);

			// Always fix location of recording
			final Point[] demoShellLocation = new Point[1];

			// Create frame visibility listener to tie into shell
			FrameVisibilityListener visiblityListener = new FrameVisibilityListener() {
				@Override
				public void notifyFrameVisibility(final boolean isVisible) {
					// Must be done on SWT thread
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							// Specify visibility of shell
							demoShell.setVisible(isVisible);

							// If visible, ensure always at same location
							synchronized (demoShellLocation) {
								if (demoShellLocation[0] != null) {
									demoShell.setLocation(demoShellLocation[0]);
								}
							}
						}
					});
				}
			};

			// Create the Demo Tool
			DemoTool demo = new DemoTool();
			demo.attachComponents(frame, visiblityListener, pane,
					new MoveActiveShellMacro());

			// Ensure Shell appears
			demoShell.setVisible(true);
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					// Just waiting
				}
			});

			// Ensure frame appears
			frame.setVisible(true);
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Just waiting
				}
			});

			// Capture location of Demo Shell
			synchronized (demoShellLocation) {
				demoShellLocation[0] = demoShell.getLocation();
			}

			// Position eclipse shell within recording area
			Rectangle recordingArea = demo.getRecordingArea();
			eclipseShell.setMaximized(false);
			eclipseShell.setBounds(recordingArea.x, recordingArea.y,
					recordingArea.width, recordingArea.height);

		} catch (Exception ex) {
			// Indicate error
			MessageDialog.openError(this.window.getShell(), "Demo", ex
					.getMessage()
					+ " [" + ex.getClass().getSimpleName() + "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Not concerned with selection changes
	}

	@Override
	public void dispose() {
		// TODO dispose the recorder (i.e. close window)
	}

}