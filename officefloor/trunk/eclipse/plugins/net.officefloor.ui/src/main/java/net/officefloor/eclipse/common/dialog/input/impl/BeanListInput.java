/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
	 * Obtains the check box image.
	 * 
	 * @param tableViewer
	 *            {@link TableViewer}.
	 * @param type
	 *            Type of image.
	 * @return {@link Image} for the check box.
	 */
	private Image getCheckboxImage(TableViewer tableViewer, boolean type) {

		/*
		 * Method code derived from:
		 * 
		 * http://tom-eclipse-dev.blogspot.com.au/2007/01/tableviewers-and-
		 * nativelooking.html
		 */

		// Obtain the control for the table viewer
		Control control = tableViewer.getControl();

		// Obtain the image registry key
		String imageRegistryKey = (type ? "CHECKED" : "UNCHECKED");

		// Lazy create the image
		Image image = JFaceResources.getImageRegistry().get(imageRegistryKey);
		if (image != null) {
			return image;
		}

		// Colour to use for transparency
		Color greenScreen = new Color(control.getDisplay(), 222, 223, 224);

		// Create shell to screen capture the image
		Shell shell = new Shell(control.getShell(), SWT.NO_TRIM
				| SWT.NO_BACKGROUND);
		shell.setBackground(greenScreen);
		Button button = new Button(shell, SWT.CHECK);
		button.setBackground(greenScreen);
		button.setSelection(type);
		Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// Stop image being stretched by width
		buttonSize.x = Math.max(buttonSize.x - 1, buttonSize.y - 1);
		buttonSize.y = Math.max(buttonSize.x - 1, buttonSize.y - 1);
		button.setSize(buttonSize);
		shell.setSize(buttonSize);

		// Remove focus highlighting
		button.setEnabled(false);

		// Open shell and take the screen shot
		shell.open();
		button.setEnabled(true); // re-enable
		while (shell.getDisplay().readAndDispatch()) {
			// Ensure open and displaying for screen shot
		}
		GC gc = new GC(shell);
		Image tempImage = new Image(control.getDisplay(), buttonSize.x,
				buttonSize.y);
		gc.copyArea(tempImage, 0, 0);
		gc.dispose();
		shell.close();

		// Make the background of image transparent
		ImageData imageData = tempImage.getImageData();
		imageData.transparentPixel = imageData.palette.getPixel(greenScreen
				.getRGB());

		// Create and register the image
		image = new Image(control.getDisplay(), imageData);
		JFaceResources.getImageRegistry().put(imageRegistryKey, image);

		// Clean up temporary image
		tempImage.dispose();

		// Return the image
		return image;
	}

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
	public Control buildControl(final InputContext context) {

		// Obtain the parent for refreshing layout
		this.parent = context.getParent();

		// Determine if require panel for including buttons
		Composite parentComposite = this.parent;
		Composite panel = null;
		if (this.isIncludeButtons) {
			// Create the panel
			panel = new Composite(this.parent, SWT.NONE);
			panel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
					false));
			panel.setLayout(new GridLayout(2, false));

			// Panel to be parent for table and buttons
			parentComposite = panel;
		}

		// Create the table
		this.table = new Table(parentComposite,
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

		// Add the columns
		String[] columnPropertyNames = new String[this.beanPropertyOrder.size()];
		CellEditor[] cellEditors = new CellEditor[this.beanPropertyOrder.size()];
		int columnIndex = 0;
		for (String propertyName : this.beanPropertyOrder) {

			// Obtain the property
			BeanProperty property = this.beanProperties.get(propertyName);

			// Add the table column for the property
			this.layout
					.addColumnData(new ColumnWeightData(property.getWeight()));
			TableColumn column = new TableColumn(this.table, SWT.NONE);
			column.setText(property.label);

			// Specify the column property name
			columnPropertyNames[columnIndex] = propertyName;

			// Provide editor for the property
			cellEditors[columnIndex] = property.createCellEditor(this.table);

			// Increment for next column
			columnIndex++;
		}

		// Create the Table Viewer
		this.tableViewer = new TableViewer(this.table);
		this.tableViewer.setUseHashlookup(true);

		// Specify the columns identifiers and editors
		this.tableViewer.setColumnProperties(columnPropertyNames);
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

			// Provide button panel
			Composite buttons = new Composite(parentComposite, SWT.NONE);
			buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
					false, false));
			buttons.setLayout(new GridLayout(1, false));

			// Provide button to add
			Button addButton = new Button(buttons, SWT.PUSH | SWT.CENTER);
			addButton.setText("+");
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
			Button deleteButton = new Button(buttons, SWT.PUSH | SWT.CENTER
					| SWT.FILL);
			deleteButton.setText("-");
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

		// Return the control
		return (panel == null ? this.table : panel);
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
		 * Creates the {@link CellEditor} for this property.
		 * 
		 * @param table
		 *            {@link Table}.
		 * @return {@link CellEditor} for this property.
		 */
		public CellEditor createCellEditor(Table table) {
			CellEditor editor;
			if (this.type == boolean.class) {
				// Check box editor
				editor = new CheckboxCellEditor(table);
			} else {
				// By default just text
				editor = new TextCellEditor(table);
			}
			return editor;
		}

		/**
		 * Obtains text to display in the cell.
		 * 
		 * @param bean
		 *            Bean to obtain the property value.
		 * @return Text to display in the cell.
		 */
		public String getCellText(B bean) {

			// No text if check box
			if (this.type == boolean.class) {
				return null;
			}

			// Provide text value
			Object value = this.getValue(bean);
			return (value == null ? "" : value.toString());
		}

		/**
		 * Obtains the image to display in the cell.
		 * 
		 * @param bean
		 *            Bean to obtain the property value.
		 * @return {@link Image} to display in the cell.
		 */
		public Image getCellImage(B bean) {

			// Provide check box image
			if (this.type == boolean.class) {
				Object value = this.getValue(bean);
				boolean isChecked = (value == null ? false : ((Boolean) value)
						.booleanValue());
				return getCheckboxImage(BeanListInput.this.tableViewer,
						isChecked);
			}

			// No image
			return null;
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
		@SuppressWarnings("unchecked")
		public Image getColumnImage(Object element, int columnIndex) {

			// Obtain the bean property
			BeanProperty beanProperty = this.getBeanProperty(columnIndex);

			// Obtain and return the cell text
			B bean = (B) element;
			return beanProperty.getCellImage(bean);
		}

		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {

			// Obtain the bean property
			BeanProperty beanProperty = this.getBeanProperty(columnIndex);

			// Obtain and return the cell text
			B bean = (B) element;
			return beanProperty.getCellText(bean);
		}

		/**
		 * Obtains the {@link BeanProperty} for the column.
		 * 
		 * @param columnIndex
		 *            Index of the column.
		 * @return {@link BeanProperty} for the column.
		 */
		private BeanProperty getBeanProperty(int columnIndex) {

			// Obtain the property name for the column
			String propertyName = BeanListInput.this.beanPropertyOrder
					.get(columnIndex);

			// Obtain the bean property
			BeanProperty beanProperty = BeanListInput.this.beanProperties
					.get(propertyName);

			// Return the bean property
			return beanProperty;
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