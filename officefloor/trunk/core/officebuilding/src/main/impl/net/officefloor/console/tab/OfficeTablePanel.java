/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.console.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * {@link JPanel} for managing a list of items.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTablePanel extends JPanel {

	/**
	 * {@link OfficeTableModel}.
	 */
	private final OfficeTableModel model;

	/**
	 * Initiate.
	 * 
	 * @param columnNames
	 *            Names of the columns.
	 */
	public OfficeTablePanel(String... columnNames) {
		this(true, false, columnNames);
	}

	/**
	 * Initiate.
	 * 
	 * @param isEditCells
	 *            <code>true</code> to allow editing the cells.
	 * @param isIncludeFileChooser
	 *            <code>true</code> to include the
	 *            {@link FileChooserTableCellEditor}.
	 * @param columnNames
	 *            Names of the columns.
	 */
	public OfficeTablePanel(boolean isEditCells, boolean isIncludeFileChooser,
			String... columnNames) {

		// Advanced panel
		GridBagLayout layoutManager = new GridBagLayout();
		this.setLayout(layoutManager);

		// Grid constraints
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.weightx = 0.99;
		constraint.fill = GridBagConstraints.HORIZONTAL;

		// Create the model
		this.model = new OfficeTableModel(isEditCells, columnNames.length);

		// Create the table with specified columns
		JTable table = new JTable(this.model, new DefaultTableColumnModel());
		int columnIndex = 0;
		for (String columnName : columnNames) {
			TableColumn column = new TableColumn(columnIndex++,
					600 / columnNames.length);
			column.setHeaderValue(columnName);
			table.addColumn(column);
		}

		// Add the file chooser button (if required)
		if (isIncludeFileChooser) {
			FileChooserTableCellEditor fileChooserCellEditor = new FileChooserTableCellEditor(
					this.model);
			TableColumn fileChooserColumn = new TableColumn(columnIndex++, 10,
					fileChooserCellEditor, fileChooserCellEditor);
			fileChooserColumn.setHeaderValue("");
			table.addColumn(fileChooserColumn);
		}

		// Add the delete button
		ButtonTableCellEditor buttonCellEditor = new ButtonTableCellEditor();
		TableColumn deleteColumn = new TableColumn(columnIndex++, 10,
				buttonCellEditor, buttonCellEditor);
		deleteColumn.setHeaderValue("");
		table.addColumn(deleteColumn);

		// Add the table (including its header)
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(table, BorderLayout.CENTER);
		this.add(tablePanel, constraint);

		// Provide button to add a row (if editing)
		if (isEditCells) {
			constraint.gridx++;
			constraint.weightx = 0.01;
			constraint.fill = GridBagConstraints.NONE;
			this.add(new JButton(new AbstractAction("add") {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Add a row to the table
					OfficeTablePanel.this.model.addRow();
				}
			}), constraint);
		}
	}

	/**
	 * Adds a row.
	 * 
	 * @param values
	 *            Values for the row.
	 */
	public void addRow(String... values) {
		this.model.addRow(values);
	}

	/**
	 * Removes a particular row.
	 * 
	 * @param rowIndex
	 *            Index of the row to remove.
	 */
	public void removeRow(int rowIndex) {
		this.model.removeRow(rowIndex);
	}

	/**
	 * Obtains the rows.
	 * 
	 * @return List of rows.
	 */
	public List<String[]> getRows() {
		return this.model.getRows();
	}

	/**
	 * <p>
	 * Handles triggering of deleting the row.
	 * <p>
	 * By default it removes the row.
	 * 
	 * @param rowIndex
	 *            Index of the row being deleted.
	 */
	protected void handleDeleteRow(int rowIndex) {
		this.removeRow(rowIndex);
	}

	/**
	 * {@link TableModel} for the {@link OfficeTablePanel}.
	 */
	private static class OfficeTableModel extends AbstractTableModel {

		/**
		 * Flag indicating if can edit the cells.
		 */
		private final boolean isEditCells;

		/**
		 * Entry value size.
		 */
		private final int entryValueSize;

		/**
		 * Rows for the model.
		 */
		private final List<String[]> rows;

		/**
		 * Initiate.
		 * 
		 * @param isEditCells
		 *            Flag indicating if can edit the cells.
		 * @param entryValueSize
		 *            Number of values per entry.
		 */
		public OfficeTableModel(boolean isEditCells, int entryValueSize) {
			this.isEditCells = isEditCells;
			this.entryValueSize = entryValueSize;
			this.rows = new ArrayList<>();
		}

		/**
		 * Add a row.
		 * 
		 * @param values
		 *            Values.
		 */
		public void addRow(String... values) {

			// Ensure have row of appropriate value size
			String[] row = new String[this.entryValueSize];
			for (int i = 0; i < row.length; i++) {
				if (i < values.length) {
					// Use provided value
					row[i] = values[i];
				} else {
					// No provided value, so default
					row[i] = null;
				}
			}

			// Add the row
			int rowIndex = this.rows.size();
			this.rows.add(row);

			// Fire row changed
			this.fireTableRowsInserted(rowIndex, rowIndex);
		}

		/**
		 * Removes the row.
		 * 
		 * @param rowIndex
		 *            Index of row to remove.
		 */
		public void removeRow(int rowIndex) {

			// Remove the row
			this.rows.remove(rowIndex);

			// Fire rows changed
			this.fireTableRowsDeleted(rowIndex, rowIndex);
		}

		/**
		 * Obtains the rows.
		 * 
		 * @return List of rows.
		 */
		public List<String[]> getRows() {
			return this.rows;
		}

		/**
		 * Specifies the value, triggering an update to the view.
		 * 
		 * @param rowIndex
		 *            Index of the row.
		 * @param columnIndex
		 *            Index of the column.
		 * @param value
		 *            Value.
		 */
		public void setValue(int rowIndex, int columnIndex, String value) {

			// Specify the value
			this.setValueAt(value, rowIndex, columnIndex);

			// Notify the value has changed
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}

		/*
		 * ================== TableModel =========================
		 */

		@Override
		public int getRowCount() {
			return this.rows.size();
		}

		@Override
		public int getColumnCount() {
			// Entry value size plus column to delete
			return this.entryValueSize + 1;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {

			// Determine if can edit cell
			if (this.isEditCells) {
				// All cells editable
				return true;
			} else {
				// Stop editing content cells (still able to delete row)
				return (columnIndex >= this.entryValueSize);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			// Obtain the row
			if (rowIndex >= this.rows.size()) {
				return null; // No row
			}
			String[] row = this.rows.get(rowIndex);

			// Obtain the column value
			if (columnIndex >= this.entryValueSize) {
				return null; // Delete button or no column
			}
			String value = row[columnIndex];

			// Return the value
			return value;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

			// Obtain the value
			String value = (aValue == null ? "" : aValue.toString());

			// Obtain the row
			if (rowIndex >= this.rows.size()) {
				return; // Unknown row
			}
			String[] row = this.rows.get(rowIndex);

			// Set the value
			if (columnIndex >= this.entryValueSize) {
				return; // Delete button or no column
			}
			row[columnIndex] = value;
		}
	}

	/**
	 * {@link TableCellEditor} for a {@link JButton}.
	 */
	private class ButtonTableCellEditor implements TableCellRenderer,
			TableCellEditor {

		/*
		 * ================ TableCellRenderer ========================
		 */

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return this.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

		/*
		 * ================ TableCellEditor ==========================
		 */

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, final int row, int column) {
			return new JButton(new AbstractAction("x") {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Handle deleting the row
					OfficeTablePanel.this.handleDeleteRow(row);
				}
			});
		}

		@Override
		public Object getCellEditorValue() {
			// No value as button to trigger an action
			return null;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			// Always editable as a button
			return true;
		}

		@Override
		public boolean stopCellEditing() {
			// Always allow editing
			return true;
		}

		@Override
		public void cancelCellEditing() {
			// Button so nothing to cancel
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			// Never able to select the cell
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			// No listeners required
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			// No listeners required
		}
	}

	/**
	 * {@link TableCellEditor} for choosing a {@link File}.
	 */
	private class FileChooserTableCellEditor implements TableCellRenderer,
			TableCellEditor {

		/**
		 * {@link OfficeTableModel}.
		 */
		private final OfficeTableModel model;

		/**
		 * Initiate.
		 * 
		 * @param model
		 *            {@link OfficeTableModel}.
		 */
		public FileChooserTableCellEditor(OfficeTableModel model) {
			this.model = model;
		}

		/*
		 * ================ TableCellRenderer ========================
		 */

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return this.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

		/*
		 * ================ TableCellEditor ==========================
		 */

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, final int row,
				final int column) {
			return new JButton(new AbstractAction("...") {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"Applications", "jar", "war");
					fileChooser.setFileFilter(filter);
					int returnVal = fileChooser
							.showOpenDialog(OfficeTablePanel.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String filePath = fileChooser.getSelectedFile()
								.getAbsolutePath();

						// Change the value (in column to left, -1)
						FileChooserTableCellEditor.this.model.setValue(row,
								column - 1, filePath);
					}
				}
			});
		}

		@Override
		public Object getCellEditorValue() {
			// No value as button to trigger an action
			return null;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			// Always editable as a button
			return true;
		}

		@Override
		public boolean stopCellEditing() {
			// Always allow editing
			return true;
		}

		@Override
		public void cancelCellEditing() {
			// Button so nothing to cancel
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			// Never able to select the cell
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			// No listeners required
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			// No listeners required
		}
	}

}