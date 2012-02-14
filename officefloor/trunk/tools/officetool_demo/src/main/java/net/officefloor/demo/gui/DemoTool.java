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

package net.officefloor.demo.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import net.officefloor.demo.macro.CommentMacro;
import net.officefloor.demo.macro.DragMacro;
import net.officefloor.demo.macro.InfoMacro;
import net.officefloor.demo.macro.InputTextMacro;
import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroSource;
import net.officefloor.demo.macro.MoveMouseMacro;
import net.officefloor.demo.macro.PauseMacro;
import net.officefloor.demo.macro.RightClickMacro;
import net.officefloor.demo.macrolist.MacroIndexFactory;
import net.officefloor.demo.macrolist.MacroItem;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.macrolist.MacroListListener;
import net.officefloor.demo.record.FrameVisibilityListener;
import net.officefloor.demo.record.RecordComponent;

public class DemoTool implements MacroIndexFactory {

	/**
	 * Default recording size.
	 */
	private static final Dimension DEFAULT_RECORDING_SIZE = new Dimension(1024,
			550);

	/**
	 * Provides ability to run the Demo Tool from command line.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) throws AWTException {

		// Create the frame
		Frame frame = new Frame("Demo Tool");

		// Attach the Demo components
		new DemoTool().attachComponents(frame, null, frame);

		// Run the application
		frame.setVisible(true);

		// Position window in middle of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(Math.abs(screenSize.width - frameSize.width) / 2,
				Math.abs(screenSize.height - frameSize.height) / 2);
	}

	/**
	 * Extension for recording file.
	 */
	public static final String RECORDING_FILE_EXTENSION = "rcd";

	/**
	 * {@link RecordComponent}.
	 */
	private RecordComponent recordComponent;

	/**
	 * {@link MacroList} containing the {@link Macro} items.
	 */
	private MacroList macros;

	/**
	 * {@link JList} for the {@link Macro} instances.
	 */
	private JList macroList;

	/**
	 * Attaches the {@link Component} instances for demo generation.
	 * 
	 * @param frame
	 *            {@link Frame} containing the {@link Container}.
	 * @param visibilityListener
	 *            {@link FrameVisibilityListener}. May be <code>null</code>.
	 * @param container
	 *            {@link Container} to attach the {@link Component} instances.
	 * @param macroSources
	 *            Optional additional {@link MacroSource} instances.
	 * @throws AWTException
	 *             If running in headless environment.
	 */
	public void attachComponents(final Frame frame,
			FrameVisibilityListener visibilityListener, Container container,
			MacroSource... macroSources) throws AWTException {

		// Ensure only attached once
		if (this.recordComponent != null) {
			throw new IllegalStateException(
					"Can only attach once to a Frame.  Please create another instance for use.");
		}

		// Always dispose on closing window
		frame.addWindowListener(new WindowAdapter() {
			private boolean isDisposed = false;

			@Override
			public void windowClosing(WindowEvent e) {
				if (!this.isDisposed) {
					// Ensure only dispose once
					frame.dispose();
					this.isDisposed = true;
				}
			}
		});

		// Create the panel
		JPanel panel = new ConfiguredPanel(true, container);

		// Create the macro listing (ensuring that they keep in sync)
		final DefaultListModel macroListModel = new DefaultListModel();
		this.macros = new MacroList(this, new MacroListListener() {
			@Override
			public void macroAdded(MacroItem item, int index) {

				// Obtain the display label for the macro
				Macro macro = item.getMacro();
				String displayLabel = macro.getDisplayLabel();
				if ((displayLabel == null)
						|| (displayLabel.trim().length() == 0)) {
					// Default to simple name
					displayLabel = macro.getClass().getSimpleName();
				}

				// Add the macro
				macroListModel.add(index, displayLabel);

				// Macro may change width so pack screen
				frame.pack();
			}

			@Override
			public void macroRemoved(MacroItem item, int index) {
				macroListModel.remove(index);

				// Macro may change width so pack screen
				frame.pack();
			}
		});

		// Create panel for recording and alignments
		JPanel recordPanel = new ConfiguredPanel(false, panel);

		// Create the record component
		this.recordComponent = new RecordComponent(new Robot(), frame,
				visibilityListener, this.macros);
		this.recordComponent.setBorder(new LineBorder(Color.RED));
		Dimension recorderSize = DEFAULT_RECORDING_SIZE;
		this.recordComponent.setMinimumSize(recorderSize);
		this.recordComponent.setPreferredSize(recorderSize);
		this.recordComponent.setMaximumSize(recorderSize);
		recordPanel.add(this.recordComponent);

		// Create marker for left menu
		JPanel markerPanel = new ConfiguredPanel(true, recordPanel);
		markerPanel.add(Box.createHorizontalStrut(100));
		markerPanel.add(new JLabel("|"));
		markerPanel.add(Box.createHorizontalGlue());

		// Add the default macro sources
		this.recordComponent.addMacro(new CommentMacro());
		this.recordComponent.addMacro(new LeftClickMacro());
		this.recordComponent.addMacro(new RightClickMacro());
		this.recordComponent.addMacro(new MoveMouseMacro());
		this.recordComponent.addMacro(new DragMacro());
		this.recordComponent.addMacro(new InputTextMacro());
		this.recordComponent.addMacro(new InfoMacro());
		this.recordComponent.addMacro(new PauseMacro());

		// Add the additional macro sources
		for (MacroSource source : macroSources) {
			this.recordComponent.addMacro(source);
		}

		// Add panel with listing of added macros
		JPanel controlPanel = new ConfiguredPanel(false, panel);

		// Provide listing of the macros
		JPanel macroPanel = new ConfiguredPanel(false, controlPanel);
		JPanel macroLabelPanel = new ConfiguredPanel(true, macroPanel);
		macroLabelPanel.add(new JLabel("Macros"));
		macroPanel.add(Box.createVerticalStrut(5));
		this.macroList = new JList(macroListModel);
		JScrollPane macroScrollPane = new JScrollPane(this.macroList);
		macroScrollPane.setBorder(new LineBorder(Color.BLACK));
		macroPanel.add(macroScrollPane);

		// Provide buttons
		controlPanel.add(Box.createVerticalStrut(20));

		// Unselect button
		JPanel unselectPanel = new ConfiguredPanel(true, controlPanel);
		JButton unselectButton = new JButton("Unselect");
		unselectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DemoTool.this.macroList.clearSelection();
			}
		});
		unselectPanel.add(unselectButton);
		unselectPanel.add(Box.createHorizontalGlue());

		controlPanel.add(Box.createVerticalStrut(20));

		// Play button
		JPanel playPanel = new ConfiguredPanel(true, controlPanel);
		playPanel.add(new PlayButton(this, frame, this.recordComponent));
		playPanel.add(Box.createHorizontalGlue());

		controlPanel.add(Box.createVerticalStrut(20));

		// Save/Open buttons
		JPanel saveOpenPanel = new ConfiguredPanel(true, controlPanel);
		saveOpenPanel.add(new SaveButton(this, frame));
		saveOpenPanel.add(new OpenButton(this, frame));

		controlPanel.add(Box.createVerticalStrut(20));

		// Delete button
		JPanel deletePanel = new ConfiguredPanel(true, controlPanel);
		deletePanel.add(new DeleteButton(this, frame));
		deletePanel.add(Box.createHorizontalGlue());

		// Organise window
		frame.pack();
	}

	/**
	 * Obtains the {@link Rectangle} providing the absolute location of the
	 * recording area.
	 * 
	 * @return {@link Rectangle} providing the absolute location of the
	 *         recording area.
	 */
	public Rectangle getRecordingArea() {
		return this.recordComponent.getRecordingArea();
	}

	/**
	 * Obtains the selected {@link Macro} instances.
	 * 
	 * @param isReturnAllIfNoneSelected
	 *            <code>true</code> indicates to return all {@link Macro}
	 *            instances if none are selected.
	 * @return Selected {@link Macro} instances.
	 */
	public Macro[] getSelectedMacros(boolean isReturnAllIfNoneSelected) {

		// Obtain the listing of selected macros
		List<Macro> selectedMacros = new LinkedList<Macro>();
		for (int index : this.macroList.getSelectedIndices()) {
			Macro macro = this.macros.getItem(index).getMacro();
			selectedMacros.add(macro);
		}
		if (isReturnAllIfNoneSelected && (selectedMacros.size() == 0)) {
			// Nothing selected, so play all macros
			for (int i = 0; i < this.macros.size(); i++) {
				Macro macro = this.macros.getItem(i).getMacro();
				selectedMacros.add(macro);
			}
		}

		// Return the macros
		return selectedMacros.toArray(new Macro[0]);
	}

	/**
	 * Obtains the indices of the selected {@link Macro} instances.
	 * 
	 * @return Indices of the selected {@link Macro} instances.
	 */
	public int[] getSelectedMacroIndices() {
		return this.macroList.getSelectedIndices();
	}

	/**
	 * Obtains the {@link MacroList}.
	 * 
	 * @return {@link MacroList}.
	 */
	public MacroList getMacroList() {
		return this.macros;
	}

	/**
	 * {@link JPanel} configured for use.
	 */
	private class ConfiguredPanel extends JPanel {

		/**
		 * Initiate.
		 * 
		 * @param isHorizontal
		 *            Layout is <code>true</code> for horizontal,
		 *            <code>false</code> for vertical.
		 * @param container
		 *            {@link Container} to add this {@link JPanel}.
		 */
		public ConfiguredPanel(boolean isHorizontal, Container container) {
			BoxLayout layout = new BoxLayout(this,
					(isHorizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
			this.setLayout(layout);
			container.add(this);
		}
	}

	/*
	 * ======================== MacroIndexFactory =======================
	 */

	@Override
	public int createMacroIndex() {

		// Determine if a selected index
		int[] selectedIndices = DemoTool.this.getSelectedMacroIndices();
		if (selectedIndices.length == 1) {
			// Add the macro after the selected macro (+1 for after)
			int index = selectedIndices[0] + 1;
			return index;
		}

		// Nothing selected so no index specified
		return -1;
	}

}