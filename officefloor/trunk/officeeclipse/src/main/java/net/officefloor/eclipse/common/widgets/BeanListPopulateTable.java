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
package net.officefloor.eclipse.common.widgets;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.OfficeFloorPluginFailure;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * {@link Table} to create and populate a list of beans.
 * 
 * @author Daniel
 */
public class BeanListPopulateTable<B> {

	/**
	 * Marker to indicate a bean.
	 */
	private static final String BEAN_MARKER = "-";

	/**
	 * {@link Table}.
	 */
	private final Table table;

	/**
	 * Type of the bean.
	 */
	private final Class<B> beanType;

	/**
	 * Order of the bean properties.
	 */
	private final List<String> beanPropertyOrder = new LinkedList<String>();

	/**
	 * Properties of the bean.
	 */
	private final Map<String, BeanProperty> beanProperties = new HashMap<String, BeanProperty>();

	/**
	 * {@link TableLayout}.
	 */
	private final TableLayout layout;

	/**
	 * Beans to be populated.
	 */
	private final List<B> beans = new LinkedList<B>();

	/**
	 * {@link TableViewer}.
	 */
	private TableViewer tableViewer;

	/**
	 * Initiate.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param beanType
	 *            Type of the bean.
	 */
	public BeanListPopulateTable(Composite parent, Class<B> beanType) {
		this.beanType = beanType;

		// Create the table
		this.table = new Table(parent, (SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION));

		// Initiate the table
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL);
		gridData.heightHint = 100;
		this.table.setLayoutData(gridData);
		this.layout = new TableLayout();
		this.table.setLayout(this.layout);
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);

		// Add the bean marker
		this.layout.addColumnData(new ColumnPixelData(10));
		new TableColumn(this.table, SWT.CENTER);
		this.beanPropertyOrder.add(BEAN_MARKER);
	}

	/**
	 * Adds property to be populated on the bean.
	 * 
	 * @param propertyName
	 *            Name of the property on the bean.
	 * @param weight
	 *            Weight for the width.
	 */
	public void addProperty(String propertyName, int weight) {

		// Add the table column for the property
		this.layout.addColumnData(new ColumnWeightData(weight));
		TableColumn column = new TableColumn(this.table, SWT.LEFT);
		column.setText(propertyName);

		// Create and add the property
		this.beanPropertyOrder.add(propertyName);
		this.beanProperties.put(propertyName, new BeanProperty(propertyName));
	}

	/**
	 * Adds a bean.
	 * 
	 * @param bean
	 *            Bean.
	 */
	public void addBean(B bean) {
		// Add to list
		BeanListPopulateTable.this.beans.add(bean);

		// Add to viewer
		if (BeanListPopulateTable.this.tableViewer != null) {
			BeanListPopulateTable.this.tableViewer.add(bean);
		}
	}

	/**
	 * <p>
	 * Generates the necessary functionality to populate the list of beans.
	 * <p>
	 * This is to be called after all properties are added via
	 * {@link #addProperty(String, int)}.
	 */
	public void generate() throws OfficeFloorPluginFailure {

		// Ensure viewer not already created
		if (this.tableViewer != null) {
			throw new OfficeFloorPluginFailure("TableViewer already created");
		}

		// Create the Table Viewer
		this.tableViewer = new TableViewer(this.table);
		this.tableViewer.setUseHashlookup(true);

		// Specify the columns (and their names)
		String[] columnNames = new String[this.beanPropertyOrder.size()];
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = this.beanPropertyOrder.get(i);
		}
		this.tableViewer.setColumnProperties(columnNames);

		// Specify the column editors
		CellEditor[] cellEditors = new CellEditor[this.beanPropertyOrder.size()];
		for (int i = 0; i < cellEditors.length; i++) {
			cellEditors[i] = new TextCellEditor(this.table);
		}
		this.tableViewer.setCellEditors(cellEditors);

		// Specify the cell modifier
		this.tableViewer.setCellModifier(new PropertyCellModifier());

		// Specify the label and content providers
		this.tableViewer.setLabelProvider(new PropertyLabelProvider());
		this.tableViewer.setContentProvider(new PropertyContentProvider());

		// Load the beans to be populated
		this.tableViewer.setInput(this.beans);

		// Provide button to add
		Button addButton = new Button(this.table.getParent(), SWT.PUSH
				| SWT.CENTER);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					// Create a new instance of the bean
					B bean = BeanListPopulateTable.this.beanType.newInstance();

					// Add the bean
					BeanListPopulateTable.this.addBean(bean);

				} catch (Exception ex) {
					throw new OfficeFloorPluginFailure(ex);
				}
			}
		});

		// Create and configure the "Delete" button
		Button deleteButton = new Button(this.table.getParent(), SWT.PUSH
				| SWT.CENTER);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));
		deleteButton.addSelectionListener(new SelectionAdapter() {

			@Override
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e) {

				// Obtain the first bean selected
				B bean = (B) ((IStructuredSelection) BeanListPopulateTable.this.tableViewer
						.getSelection()).getFirstElement();

				// Remove the bean
				BeanListPopulateTable.this.beans.remove(bean);

				// Remove from view
				BeanListPopulateTable.this.tableViewer.remove(bean);
			}
		});
	}

	/**
	 * Obtains the beans.
	 * 
	 * @return Beans.
	 */
	@SuppressWarnings("unchecked")
	public List<B> getBeans() {
		return this.beans;
	}

	/**
	 * Property of the bean.
	 */
	private class BeanProperty {

		/**
		 * Name of the property.
		 */
		public final String name;

		/**
		 * Access {@link Method}.
		 */
		private final Method accessor;

		/**
		 * Mutator {@link Method}.
		 */
		private final Method mutator;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Property Name.
		 */
		public BeanProperty(String name) throws OfficeFloorPluginFailure {
			this.name = name;

			// Transform name to method name
			String methodName = name.replace(" ", "");
			methodName = methodName.substring(0, 1).toUpperCase()
					+ methodName.substring(1);

			try {
				// Find the accessor and mutator methods
				this.accessor = BeanListPopulateTable.this.beanType
						.getMethod("get" + methodName);
				this.mutator = BeanListPopulateTable.this.beanType.getMethod(
						"set" + methodName, new Class[] { String.class });
			} catch (NoSuchMethodException ex) {
				throw new OfficeFloorPluginFailure(ex);
			}
		}

		/**
		 * Obtains the property value from the bean.
		 * 
		 * @param bean
		 *            Bean to obtain the property value.
		 * @return Value of the bean's property.
		 */
		public String getValue(B bean) throws OfficeFloorPluginFailure {
			try {
				// Obtain the return of the bean's accessor
				Object value = this.accessor.invoke(bean);

				// Return the value
				return (value == null ? "" : value.toString());

			} catch (Exception ex) {
				throw new OfficeFloorPluginFailure(ex);
			}
		}

		/**
		 * Specifies the property value on the bean.
		 * 
		 * @param bean
		 *            Bean to have its property specified.
		 * @param value
		 *            Value to set on the property of the bean.
		 */
		public void setValue(B bean, String value)
				throws OfficeFloorPluginFailure {
			try {
				// Set the property value on the bean
				this.mutator.invoke(bean, value);
			} catch (Exception ex) {
				throw new OfficeFloorPluginFailure(ex);
			}
		}
	}

	/**
	 * {@link ICellModifier} to indicate change of property.
	 */
	private class PropertyCellModifier implements ICellModifier {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
		 *      java.lang.String)
		 */
		@Override
		public boolean canModify(Object element, String property) {
			// Change all properties except the marker
			return !BEAN_MARKER.equals(property);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
		 *      java.lang.String)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object getValue(Object element, String property) {

			// Obtain the bean property
			BeanProperty beanProperty = BeanListPopulateTable.this.beanProperties
					.get(property);

			// Obtain the property value
			B bean = (B) element;
			String value = beanProperty.getValue(bean);

			// Return the value
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
		 *      java.lang.String, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void modify(Object element, String property, Object value) {

			// Obtain the bean
			if (element instanceof TableItem) {
				element = ((TableItem) element).getData();
			}

			// Obtain the bean property
			BeanProperty beanProperty = BeanListPopulateTable.this.beanProperties
					.get(property);

			// Set the property
			B bean = (B) element;
			beanProperty.setValue(bean, value.toString());

			// Update the view with changes
			BeanListPopulateTable.this.tableViewer.update(bean,
					new String[] { property });
		}
	}

	/**
	 * Property {@link ITableLabelProvider}.
	 */
	private class PropertyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {

			// Obtain the property name for the column
			String propertyName = BeanListPopulateTable.this.beanPropertyOrder
					.get(columnIndex);

			// Return the marker if the marker column
			if (BEAN_MARKER.equals(propertyName)) {
				return BEAN_MARKER;
			}

			// Obtain the bean property
			BeanProperty beanProperty = BeanListPopulateTable.this.beanProperties
					.get(propertyName);

			// Obtain the value
			B bean = (B) element;
			String value = beanProperty.getValue(bean);

			// Return the value
			return value;
		}
	}

	/**
	 * Property {@link IStructuredContentProvider}.
	 */
	private class PropertyContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			// Return the beans
			return BeanListPopulateTable.this.beans.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing
		}
	}
}
