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

package net.officefloor.eclipse.common.dialog.input.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.util.LogUtil;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * {@link Table} to create and populate a list of beans.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanListInput<B> implements Input<Control> {

	/**
	 * Marker to indicate a bean.
	 */
	private static final String BEAN_MARKER = "-";

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
	 * Beans to be populated.
	 */
	private final List<B> beans = new LinkedList<B>();

	/**
	 * Indicates to include the buttons.
	 */
	private final boolean isIncludeButtons;

	/**
	 * Parent {@link Composite}.
	 */
	private Composite parent;

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
	 * @param beanType
	 *            Type of the bean.
	 */
	public BeanListInput(Class<B> beanType) {
		this(beanType, true);
	}

	/**
	 * Initiate.
	 * 
	 * @param beanType
	 *            Type of the bean.
	 * @param isIncludeButtons
	 *            Indicates if should include the buttons.
	 */
	public BeanListInput(Class<B> beanType, boolean isIncludeButtons) {
		this.beanType = beanType;
		this.isIncludeButtons = isIncludeButtons;

		// Bean marker always the first column
		this.beanPropertyOrder.add(BEAN_MARKER);
	}

	/**
	 * <p>
	 * Adds property to be populated on the bean.
	 * <p>
	 * This may NOT be called after {@link #buildControl(InputContext)}.
	 * 
	 * @param propertyName
	 *            Name of the property on the bean.
	 * @param weight
	 *            Weight for the width.
	 */
	public void addProperty(String propertyName, int weight) {
		this.addProperty(propertyName, weight, null);
	}

	/**
	 * <p>
	 * Adds property to be populated on the bean.
	 * <p>
	 * This may NOT be called after {@link #buildControl(InputContext)}.
	 * 
	 * @param propertyName
	 *            Name of the property on the bean.
	 * @param weight
	 *            Weight for the width.
	 * @param label
	 *            Label for column header of property.
	 */
	public void addProperty(String propertyName, int weight, String label) {

		// Ensure properties are not added after building control
		if (this.tableViewer != null) {
			LogUtil.logError("Can not add properties after building table (property: "
					+ propertyName + ")");
			return;
		}

		// Add the property
		this.beanPropertyOrder.add(propertyName);
		this.beanProperties.put(propertyName, new BeanProperty(propertyName,
				weight, label));
	}

	/**
	 * Adds a bean.
	 * 
	 * @param bean
	 *            Bean.
	 */
	public void addBean(B bean) {
		// Add to list
		this.beans.add(bean);

		// Add to viewer
		if (this.tableViewer != null) {
			this.tableViewer.add(bean);
		}

		// Layout change
		if (this.parent != null) {
			this.parent.layout();
		}
	}

	/**
	 * Removes a bean.
	 * 
	 * @param bean
	 *            Bean.
	 */
	public void removeBean(B bean) {
		// Remove from list
		this.beans.remove(bean);

		// Remove from viewer
		if (this.tableViewer != null) {
			this.tableViewer.remove(bean);
		}

		// Layout change
		if (this.parent != null) {
			this.parent.layout();
		}
	}

	/**
	 * Clears all the beans.
	 */
	public void clearBeans() {
		for (B bean : new ArrayList<B>(this.beans)) {
			this.removeBean(bean);
		}
	}

	/*
	 * ==================== Input ==========================================
	 */

	@Override
	public Table buildControl(final InputContext context) {

		// Obtain the parent for refreshing layout
		this.parent = context.getParent();

		// Create the table
		this.table = new Table(this.parent,
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

		// Add the property columns
		for (String propertyName : this.beanPropertyOrder) {

			// Determine if a bean marker
			if (BEAN_MARKER.equals(propertyName)) {
				// Add the bean marker column
				this.layout.addColumnData(new ColumnPixelData(10));
				new TableColumn(this.table, SWT.CENTER);
			} else {
				// Obtain the property
				BeanProperty property = this.beanProperties.get(propertyName);

				// Add the table column for the property
				this.layout.addColumnData(new ColumnWeightData(property
						.getWeight()));
				TableColumn column = new TableColumn(this.table, SWT.LEFT);
				column.setText(property.label);
			}
		}

		// Create the Table Viewer
		this.tableViewer = new TableViewer(this.table);
		this.tableViewer.setUseHashlookup(true);

		// Specify the columns
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
		this.tableViewer.setCellModifier(new PropertyCellModifier(context));

		// Specify the label and content providers
		this.tableViewer.setLabelProvider(new PropertyLabelProvider());
		this.tableViewer.setContentProvider(new PropertyContentProvider());

		// Load the beans to be populated
		this.tableViewer.setInput(this.beans);

		// Determine if to include the buttons
		if (this.isIncludeButtons) {
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
						B bean = BeanListInput.this.beanType.newInstance();

						// Add the bean
						BeanListInput.this.addBean(bean);

						// Notify changed
						context.notifyValueChanged(BeanListInput.this.beans);

					} catch (Exception ex) {
						LogUtil.logError("Failed to add bean", ex);
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
					B bean = (B) ((IStructuredSelection) BeanListInput.this.tableViewer
							.getSelection()).getFirstElement();
					if (bean == null) {
						// Nothing selected to delete
						return;
					}

					// Remove the bean
					BeanListInput.this.removeBean(bean);

					// Notify changed
					context.notifyValueChanged(BeanListInput.this.beans);
				}
			});
		}

		// Return the table
		return this.table;
	}

	@Override
	public List<B> getValue(Control control, InputContext context) {
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
		 * Weight of the column width for this property.
		 */
		private final int weight;

		/**
		 * Label for column header of property.
		 */
		public final String label;

		/**
		 * Type of the property.
		 */
		private final Class<?> type;

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
		 * @param weight
		 *            Weight of the column width for this property.
		 * @param label
		 *            Label for column header of property.
		 */
		public BeanProperty(String name, int weight, String label) {
			this.name = name;
			this.weight = weight;
			this.label = (label == null ? this.name : label);

			// Transform name to method name
			String methodName = name.replace(" ", "");
			methodName = methodName.substring(0, 1).toUpperCase()
					+ methodName.substring(1);

			// Attempt to obtain the accessor (must have)
			Method accessMethod = null;
			try {
				accessMethod = BeanListInput.this.beanType.getMethod("get"
						+ methodName);
			} catch (Exception ex) {
				// Must have accessor
				LogUtil.logError("Must have accessor for '" + methodName
						+ "' on bean " + BeanListInput.this.beanType.getName());
			}
			this.accessor = accessMethod;

			// Obtain the property type (defaults to string)
			this.type = (this.accessor == null ? String.class : this.accessor
					.getReturnType());

			// Attempt to obtain the mutator (not necessary)
			Method method = null;
			try {
				method = BeanListInput.this.beanType.getMethod("set"
						+ methodName, new Class[] { this.type });
			} catch (Exception ex) {
				// No mutator
			}
			this.mutator = method;
		}

		/**
		 * Obtains the weight of the column width for this property.
		 * 
		 * @return Weight of the column width for this property.
		 */
		public int getWeight() {
			return this.weight;
		}

		/**
		 * Obtains the property value from the bean.
		 * 
		 * @param bean
		 *            Bean to obtain the property value.
		 * @return Value of the bean's property.
		 */
		public Object getValue(B bean) {
			try {
				// Obtain the return of the bean's accessor
				Object value = this.accessor.invoke(bean);

				// Return the value
				return value;

			} catch (Exception ex) {
				LogUtil.logError("Failed to get value for property "
						+ this.name, ex);
				return "";
			}
		}

		/**
		 * Indicates if can modify this property.
		 * 
		 * @return <code>true</code> if can modify this property.
		 */
		public boolean canModify() {
			return (this.mutator != null);
		}

		/**
		 * Specifies the property value on the bean.
		 * 
		 * @param bean
		 *            Bean to have its property specified.
		 * @param value
		 *            Value to set on the property of the bean.
		 */
		public void setValue(B bean, Object value) {

			// Only set value if have mutator
			if (this.mutator == null) {
				return;
			}

			try {
				// Set the property value on the bean
				this.mutator.invoke(bean, value);
			} catch (Exception ex) {
				LogUtil.logError("Failed to set value for property "
						+ this.name, ex);
			}
		}
	}

	/**
	 * {@link ICellModifier} to indicate change of property.
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
		 * ============= ICellModifier ===========================
		 */

		@Override
		public boolean canModify(Object element, String property) {

			// Can not change if the marker
			if (BEAN_MARKER.equals(property)) {
				return false;
			}

			// Obtain the bean property
			BeanProperty beanProperty = BeanListInput.this.beanProperties
					.get(property);

			// Return whether the property can change
			return beanProperty.canModify();
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object getValue(Object element, String property) {

			// Obtain the bean property
			BeanProperty beanProperty = BeanListInput.this.beanProperties
					.get(property);

			// Obtain the property value
			B bean = (B) element;
			Object value = beanProperty.getValue(bean);

			// Return the value
			return value;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void modify(Object element, String property, Object value) {

			// Obtain the bean
			if (element instanceof TableItem) {
				element = ((TableItem) element).getData();
			}

			// Obtain the bean property
			BeanProperty beanProperty = BeanListInput.this.beanProperties
					.get(property);

			// Set the property
			B bean = (B) element;
			beanProperty.setValue(bean, value);

			// Update the view with changes
			BeanListInput.this.tableViewer.update(bean,
					new String[] { property });

			// Notify change
			this.inputContext.notifyValueChanged(BeanListInput.this.beans);

			// Layout the change
			BeanListInput.this.parent.layout();
		}
	}

	/**
	 * Property {@link ITableLabelProvider}.
	 */
	private class PropertyLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * =============== ITableLabelProvider =======================
		 */

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {

			// Obtain the property name for the column
			String propertyName = BeanListInput.this.beanPropertyOrder
					.get(columnIndex);

			// Return the marker if the marker column
			if (BEAN_MARKER.equals(propertyName)) {
				return BEAN_MARKER;
			}

			// Obtain the bean property
			BeanProperty beanProperty = BeanListInput.this.beanProperties
					.get(propertyName);

			// Obtain the value
			B bean = (B) element;
			Object value = beanProperty.getValue(bean);

			// Return the value
			return (value == null ? "" : value.toString());
		}
	}

	/**
	 * Property {@link IStructuredContentProvider}.
	 */
	private class PropertyContentProvider implements IStructuredContentProvider {

		/*
		 * ============== IStructuredContentProvider ========================
		 */

		@Override
		public Object[] getElements(Object inputElement) {
			// Return the beans
			return BeanListInput.this.beans.toArray();
		}

		@Override
		public void dispose() {
			// Do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing
		}
	}

}
