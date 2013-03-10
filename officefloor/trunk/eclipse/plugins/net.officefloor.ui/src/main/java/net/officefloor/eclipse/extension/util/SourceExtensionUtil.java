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
package net.officefloor.eclipse.extension.util;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathFileInput;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtension;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtensionContext;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class providing helper methods for source extension implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceExtensionUtil {

	/**
	 * Loads the property layout for creating property displays.
	 * 
	 * @param container
	 *            {@link Composite} to contain the properties.
	 */
	public static void loadPropertyLayout(Composite container) {
		container.setLayout(new GridLayout(2, false));
	}

	/**
	 * Creates the display to inform that no {@link Property} instances are
	 * required.
	 * 
	 * @param container
	 *            {@link Composite} to provide display on.
	 */
	public static void informNoPropertiesRequired(Composite container) {
		container.setLayout(new GridLayout(1, false));
		new Label(container, SWT.NONE).setText("No properties required");
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyClass(String label, String name,
			Composite container, WorkSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyClass(label, name, container, new WorkGeneric(
				context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyClass(String label, String name,
			Composite container, ManagedObjectSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyClass(label, name, container,
				new ManagedObjectGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GovernanceSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyClass(String label, String name,
			Composite container, GovernanceSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyClass(label, name, container,
				new GovernanceGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyClass(String label, String name,
			Composite container, SectionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyClass(label, name, container, new SectionGeneric(
				context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	private static Property createPropertyClass(String label, String name,
			Composite container, final GenericSourceExtensionContext context,
			final PropertyValueChangeListener listener) {

		// Obtain the property
		final Property property = context.getPropertyList().getOrAddProperty(
				name);

		// Create the input to obtain the class
		ClasspathClassInput input = new ClasspathClassInput(
				context.getProject(), container.getShell());

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the input to specify value
		InputHandler<String> handler = new InputHandler<String>(container,
				input, property.getValue(), new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						String propertyValue = (String) value;
						property.setValue(propertyValue);
						if (listener != null) {
							listener.propertyValueChanged(new PropertyValueChangeEventImpl(
									property));
						}
						context.notifyPropertiesChanged();
					}

					@Override
					public void notifyValueInvalid(String message) {
						property.setValue("");
						context.notifyPropertiesChanged();
					}
				});
		handler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Return the property
		return property;
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyResource(String label, String name,
			Composite container, WorkSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyResource(label, name, container, new WorkGeneric(
				context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyResource(String label, String name,
			Composite container, ManagedObjectSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyResource(label, name, container,
				new ManagedObjectGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	private static Property createPropertyResource(String label, String name,
			Composite container, final GenericSourceExtensionContext context,
			final PropertyValueChangeListener listener) {

		// Obtain the property
		final Property property = context.getPropertyList().getOrAddProperty(
				name);

		// Create the input to obtain the resource
		ClasspathFileInput input = new ClasspathFileInput(context.getProject(),
				container.getShell());

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the input to specify value
		InputHandler<String> handler = new InputHandler<String>(container,
				input, property.getValue(), new InputListener() {
					@Override
					public void notifyValueChanged(Object value) {
						String propertyValue = (String) value;
						property.setValue(propertyValue);
						if (listener != null) {
							listener.propertyValueChanged(new PropertyValueChangeEventImpl(
									property));
						}
						context.notifyPropertiesChanged();
					}

					@Override
					public void notifyValueInvalid(String message) {
						property.setValue("");
						context.notifyPropertiesChanged();
					}
				});
		handler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Return the property
		return property;
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyText(String label, String name,
			String defaultValue, Composite container,
			final ManagedObjectSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyText(label, name, defaultValue, container,
				new ManagedObjectGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyText(String label, String name,
			String defaultValue, Composite container,
			WorkSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyText(label, name, defaultValue, container,
				new WorkGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyText(String label, String name,
			String defaultValue, Composite container,
			SectionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyText(label, name, defaultValue, container,
				new SectionGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	private static Property createPropertyText(String label, String name,
			String defaultValue, Composite container,
			final GenericSourceExtensionContext context,
			final PropertyValueChangeListener listener) {

		// Obtain the property
		final Property property = context.getPropertyList().getOrAddProperty(
				name);

		// Default the property value if blank
		String propertyValue = property.getValue();
		if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
			property.setValue(defaultValue == null ? "" : defaultValue);
		}

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the text to specify value
		final Text text = new Text(container, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		text.setText(property.getValue());
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				// Determine if have property value
				String propertyValue = text.getText();
				if (EclipseUtil.isBlank(propertyValue)) {
					// Set to null if blank
					propertyValue = null;
				}

				// Load the property value
				property.setValue(propertyValue);
				if (listener != null) {
					listener.propertyValueChanged(new PropertyValueChangeEventImpl(
							property));
				}
				context.notifyPropertiesChanged();
			}
		});

		// Return the property
		return property;
	}

	/**
	 * Creates the display for the input {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @param hidePropertyNames
	 *            {@link Property} names to hide.
	 */
	public static void createPropertyList(String label, Composite container,
			final ManagedObjectSourceExtensionContext context,
			String... hidePropertyNames) {
		createPropertyList(label, container, new ManagedObjectGeneric(context),
				hidePropertyNames);
	}

	/**
	 * Creates the display for the input {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @param hidePropertyNames
	 *            {@link Property} names to hide.
	 */
	public static void createPropertyList(String label, Composite container,
			WorkSourceExtensionContext context, String... hidePropertyNames) {
		createPropertyList(label, container, new WorkGeneric(context),
				hidePropertyNames);
	}

	/**
	 * Creates the display for the input {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 * @param hidePropertyNames
	 *            {@link Property} names to hide.
	 */
	public static void createPropertyList(String label, Composite container,
			SectionSourceExtensionContext context, String... hidePropertyNames) {
		createPropertyList(label, container, new SectionGeneric(context),
				hidePropertyNames);
	}

	/**
	 * Creates the display for the input {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param hidePropertyNames
	 *            {@link Property} names to hide.
	 */
	private static void createPropertyList(String label, Composite container,
			final GenericSourceExtensionContext context,
			String... hidePropertyNames) {

		// Obtain the property list
		final PropertyList properties = context.getPropertyList();

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the table to load property list
		PropertyListInput propertyListInput = new PropertyListInput(properties);
		for (String hidePropertyName : hidePropertyNames) {
			propertyListInput.hideProperty(hidePropertyName);
		}
		new InputHandler<PropertyList>(container, propertyListInput,
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						context.notifyPropertiesChanged();
					}
				});
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param trueValue
	 *            Value should {@link Property} be checked.
	 * @param falseValue
	 *            Value should {@link Property} be unchecked.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link ManagedObjectSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCheckbox(String label, String name,
			boolean defaultValue, String trueValue, String falseValue,
			Composite container, ManagedObjectSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCheckbox(label, name, defaultValue, trueValue,
				falseValue, container, new ManagedObjectGeneric(context),
				listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param trueValue
	 *            Value should {@link Property} be checked.
	 * @param falseValue
	 *            Value should {@link Property} be unchecked.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WorkSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCheckbox(String label, String name,
			boolean defaultValue, String trueValue, String falseValue,
			Composite container, WorkSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCheckbox(label, name, defaultValue, trueValue,
				falseValue, container, new WorkGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param trueValue
	 *            Value should {@link Property} be checked.
	 * @param falseValue
	 *            Value should {@link Property} be unchecked.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCheckbox(String label, String name,
			boolean defaultValue, String trueValue, String falseValue,
			Composite container, SectionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCheckbox(label, name, defaultValue, trueValue,
				falseValue, container, new SectionGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param trueValue
	 *            Value should {@link Property} be checked.
	 * @param falseValue
	 *            Value should {@link Property} be unchecked.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	private static Property createPropertyCheckbox(String label, String name,
			boolean defaultValue, final String trueValue,
			final String falseValue, Composite container,
			final GenericSourceExtensionContext context,
			final PropertyValueChangeListener listener) {

		// Obtain the property
		final Property property = context.getPropertyList().getOrAddProperty(
				name);

		// Determine if checked
		boolean isChecked;
		if (trueValue.equals(property.getValue())) {
			isChecked = true;
		} else if (falseValue.equals(property.getValue())) {
			isChecked = false;
		} else {
			isChecked = defaultValue;
		}

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the check box to specify value
		final Button checkbox = new Button(container, SWT.CHECK);
		checkbox.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		checkbox.setSelection(isChecked);
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				property.setValue(checkbox.getSelection() ? trueValue
						: falseValue);
				if (listener != null) {
					listener.propertyValueChanged(new PropertyValueChangeEventImpl(
							property));
				}
				context.notifyPropertiesChanged();
			}
		});

		// Return the property
		return property;
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param values
	 *            Values to list in the {@link Combo}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCombo(String label, String name,
			String defaultValue, String[] values, Composite container,
			SectionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCombo(label, name, defaultValue, values,
				container, new SectionGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link Property}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param name
	 *            Name of the {@link Property}.
	 * @param defaultValue
	 *            Default value for the {@link Property}.
	 * @param values
	 *            Values to list in the {@link Combo}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link GenericSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	private static Property createPropertyCombo(String label, String name,
			String defaultValue, String[] values, Composite container,
			final GenericSourceExtensionContext context,
			final PropertyValueChangeListener listener) {

		// Obtain the property
		final Property property = context.getPropertyList().getOrAddProperty(
				name);

		// Default the property value if blank
		String propertyValue = property.getValue();
		if ((propertyValue == null) || (propertyValue.trim().length() == 0)) {
			property.setValue(defaultValue == null ? "" : defaultValue);
		}

		// Provide the label
		new Label(container, SWT.NONE).setText(label + ": ");

		// Provide the combo box to specify value
		final Combo combo = new Combo(container, SWT.SIMPLE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		for (String value : values) {
			combo.add(value);
		}
		combo.setText(property.getValue());
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				property.setValue(combo.getText());
				if (listener != null) {
					listener.propertyValueChanged(new PropertyValueChangeEventImpl(
							property));
				}
				context.notifyPropertiesChanged();
			}
		});

		// Return the property
		return property;
	}

	/**
	 * All access via static methods.
	 */
	private SourceExtensionUtil() {
	}

	/**
	 * {@link PropertyValueChangeEvent} implementation.
	 */
	private static class PropertyValueChangeEventImpl implements
			PropertyValueChangeEvent {

		/**
		 * {@link Property}.
		 */
		private final Property property;

		/**
		 * Initiate.
		 * 
		 * @param property
		 *            {@link Property}.
		 */
		public PropertyValueChangeEventImpl(Property property) {
			this.property = property;
		}

		/*
		 * ================== PropertyValueChangeEvent ===================
		 */

		@Override
		public Property getProperty() {
			return this.property;
		}
	}

	/**
	 * Generic source extension context.
	 */
	private static interface GenericSourceExtensionContext {

		/**
		 * Obtains the {@link IProject}.
		 * 
		 * @return {@link IProject}.
		 */
		IProject getProject();

		/**
		 * Obtains the {@link PropertyList}.
		 * 
		 * @return {@link PropertyList}.
		 */
		PropertyList getPropertyList();

		/**
		 * Notifies the properties have changed.
		 */
		void notifyPropertiesChanged();
	}

	/**
	 * {@link WorkSourceExtension} {@link GenericSourceExtensionContext}.
	 */
	private static class WorkGeneric implements GenericSourceExtensionContext {

		/**
		 * {@link WorkSourceExtensionContext}.
		 */
		private final WorkSourceExtensionContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link WorkSourceExtensionContext}.
		 */
		public WorkGeneric(WorkSourceExtensionContext context) {
			this.context = context;
		}

		/*
		 * =============== GenericSourceExtensionContext ====================
		 */

		@Override
		public IProject getProject() {
			return this.context.getProject();
		}

		@Override
		public PropertyList getPropertyList() {
			return this.context.getPropertyList();
		}

		@Override
		public void notifyPropertiesChanged() {
			this.context.notifyPropertiesChanged();
		}
	}

	/**
	 * {@link ManagedObjectSourceExtension}
	 * {@link GenericSourceExtensionContext}.
	 */
	private static class ManagedObjectGeneric implements
			GenericSourceExtensionContext {

		/**
		 * {@link ManagedObjectSourceExtensionContext}.
		 */
		private final ManagedObjectSourceExtensionContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link ManagedObjectSourceExtensionContext}.
		 */
		public ManagedObjectGeneric(ManagedObjectSourceExtensionContext context) {
			this.context = context;
		}

		/*
		 * =============== GenericSourceExtensionContext ====================
		 */

		@Override
		public IProject getProject() {
			return this.context.getProject();
		}

		@Override
		public PropertyList getPropertyList() {
			return this.context.getPropertyList();
		}

		@Override
		public void notifyPropertiesChanged() {
			this.context.notifyPropertiesChanged();
		}
	}

	/**
	 * {@link SectionSourceExtensionContext}
	 * {@link GenericSourceExtensionContext}.
	 */
	private static class SectionGeneric implements
			GenericSourceExtensionContext {

		/**
		 * {@link SectionSourceExtensionContext}.
		 */
		private final SectionSourceExtensionContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link SectionSourceExtensionContext}.
		 */
		public SectionGeneric(SectionSourceExtensionContext context) {
			this.context = context;
		}

		/*
		 * =============== GenericSourceExtensionContext ====================
		 */

		@Override
		public IProject getProject() {
			return this.context.getProject();
		}

		@Override
		public PropertyList getPropertyList() {
			return this.context.getPropertyList();
		}

		@Override
		public void notifyPropertiesChanged() {
			this.context.notifyPropertiesChanged();
		}
	}

	/**
	 * {@link GovernanceSourceExtension} {@link GenericSourceExtensionContext}.
	 */
	private static class GovernanceGeneric implements
			GenericSourceExtensionContext {

		/**
		 * {@link GovernanceSourceExtensionContext}.
		 */
		private final GovernanceSourceExtensionContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link GovernanceSourceExtensionContext}.
		 */
		public GovernanceGeneric(GovernanceSourceExtensionContext context) {
			this.context = context;
		}

		/*
		 * =============== GenericSourceExtensionContext ====================
		 */

		@Override
		public IProject getProject() {
			return this.context.getProject();
		}

		@Override
		public PropertyList getPropertyList() {
			return this.context.getPropertyList();
		}

		@Override
		public void notifyPropertiesChanged() {
			this.context.notifyPropertiesChanged();
		}
	}

}