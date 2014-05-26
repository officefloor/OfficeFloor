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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
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
	 */
	public OfficeTablePanel(String... columnNames) {

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
		this.model = new OfficeTableModel(columnNames.length);

		// Add the table (including its header)
		JTable table = new JTable(this.model, new DefaultTableColumnModel());
		for (String columnName : columnNames) {
			TableColumn column = new TableColumn();
			column.setHeaderValue(columnName);
			table.addColumn(column);
		}
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(table, BorderLayout.CENTER);
		this.add(tablePanel, constraint);

		// Provide button to add a row
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
	 * {@link TableModel} for the {@link OfficeTablePanel}.
	 */
	private static class OfficeTableModel extends AbstractTableModel {

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
		 * @param entryValueSize
		 *            Number of values per entry.
		 */
		public OfficeTableModel(int entryValueSize) {
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
			// TODO Auto-generated method stub
			super.setValueAt(aValue, rowIndex, columnIndex);
		}
	}

}