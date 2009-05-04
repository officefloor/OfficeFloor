/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.dialog.input.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.util.EclipseUtil;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * {@link PropertyList} {@link Input}.
 * 
 * @author Daniel
 */
public class PropertyListInput implements Input<Table> {

	/**
	 * Name column property.
	 */
	private static final String NAME_COLUMN = "Name";

	/**
	 * Value column property.
	 */
	private static final String VALUE_COLUMN = "Value";

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link Table}.
	 */
	private Table table;

	/**
	 * {@link TableLayout}.
	 */
	private TableLayout layout;

	/**
	 * {@link TableViewer}.
	 */
	private TableViewer tableViewer;

	/**
	 * Initiate.
	 * 
	 * @param propertyList
	 *            {@link PropertyList}.
	 */
	public PropertyListInput(PropertyList propertyList) {
		this.propertyList = propertyList;
	}

	/*
	 * ====================== Input ===========================================
	 */

	@Override
	public Table buildControl(InputContext context) {

		// Create the table
		this.table = new Table(context.getParent(),
				(SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
						| SWT.FULL_SELECTION | SWT.HIDE_SELECTION));

		// Initiate the table
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL);
		gridData.heightHint = 100;
		this.table.setLayoutData(gridData);
		this.layout = new TableLayout();
		this.table.setLayout(this.layout);
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);

		// Add name column
		this.layout.addColumnData(new ColumnWeightData(1));
		TableColumn nameColumn = new TableColumn(this.table, SWT.LEFT);
		nameColumn.setText(NAME_COLUMN);

		// Add value column
		this.layout.addColumnData(new ColumnWeightData(2));
		TableColumn valueColumn = new TableColumn(this.table, SWT.LEFT);
		valueColumn.setText(VALUE_COLUMN);

		// Create the Table Viewer
		this.tableViewer = new TableViewer(this.table);
		this.tableViewer.setUseHashlookup(true);

		// Specify the column properties
		this.tableViewer.setColumnProperties(new String[] { NAME_COLUMN,
				VALUE_COLUMN });

		// Specify the column editors
		this.tableViewer
				.setCellEditors(new CellEditor[] {
						new TextCellEditor(this.table),
						new TextCellEditor(this.table) });

		// Specify the cell modifier
		this.tableViewer.setCellModifier(new PropertyCellModifier(context));

		// Specify the label and content providers
		this.tableViewer.setLabelProvider(new PropertyLabelProvider());
		this.tableViewer.setContentProvider(new PropertyContentProvider());

		// Load the property list to be populated
		this.tableViewer.setInput(this.propertyList);

		// TODO provide ability to add/delete properties

		// Return the table
		return this.table;
	}

	@Override
	public Object getValue(Table control, InputContext context) {
		return this.propertyList;
	}

	/**
	 * {@link ICellModifier} to indicate change of {@link Property}.
	 */
	private class PropertyCellModifier implements ICellModifier {

		/**
		 * {@link InputContext}.
		 */
		private final InputContext inputContext;

		/**
		 * Initiate.
		 * 
		 * @param inputContext
		 *            {@link InputContext}.
		 */
		public PropertyCellModifier(InputContext inputContext) {
			this.inputContext = inputContext;
		}

		/*
		 * ===================== ICellModifier =============================
		 */

		@Override
		public boolean canModify(Object element, String property) {
			// May only modify the value column
			return VALUE_COLUMN.equals(property);
		}

		@Override
		public Object getValue(Object element, String property) {
			Property propertyEntry = (Property) element;
			if (NAME_COLUMN.equals(property)) {
				String label = propertyEntry.getLabel();
				if (EclipseUtil.isBlank(label)) {
					label = propertyEntry.getName();
				}
				return label;

			} else if (VALUE_COLUMN.equals(property)) {
				return propertyEntry.getValue();
			}

			// Unknown column
			return "Unknown column " + property;
		}

		@Override
		public void modify(Object element, String property, Object value) {

			// May only change the value entry
			if (!(VALUE_COLUMN.equals(property))) {
				return; // may not change
			}

			// Obtain the property
			if (element instanceof TableItem) {
				element = ((TableItem) element).getData();
			}
			Property propertyEntry = (Property) element;

			// Obtain the property value
			String propertyValue = (value == null ? null : value.toString());

			// Specify property value and notify of change
			propertyEntry.setValue(propertyValue);

			// Update the view with changes
			PropertyListInput.this.tableViewer.update(propertyEntry,
					new String[] { property });

			// Notify change
			this.inputContext
					.notifyValueChanged(PropertyListInput.this.propertyList);
		}
	}

	/**
	 * {@link Property} {@link ITableLabelProvider}.
	 */
	private class PropertyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * =============== ITableLabelProvider ==========================
		 */

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {

			// Obtain the property
			Property property = (Property) element;

			// Provide value from property
			switch (columnIndex) {
			case 0:
				String label = property.getLabel();
				if (EclipseUtil.isBlank(label)) {
					label = property.getName();
				}
				return label;

			case 1:
				return property.getValue();

			default:
				return "Unknown column index " + columnIndex;
			}
		}
	}

	/**
	 * {@link Property} {@link IStructuredContentProvider}.
	 */
	private class PropertyContentProvider implements IStructuredContentProvider {

		/*
		 * ================ IStructuredContentProvider ====================
		 */

		@Override
		public Object[] getElements(Object inputElement) {

			// Obtain the listing of properties
			List<Property> properties = new LinkedList<Property>();
			for (Property property : PropertyListInput.this.propertyList) {
				properties.add(property);
			}

			// Return the property instances
			return properties.toArray(new Property[0]);
		}

		@Override
		public void dispose() {
			// Do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing, as should not change
		}
	}

}