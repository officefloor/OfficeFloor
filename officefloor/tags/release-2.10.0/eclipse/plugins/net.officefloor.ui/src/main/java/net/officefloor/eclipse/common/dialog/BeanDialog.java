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
package net.officefloor.eclipse.common.dialog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.InvalidValueException;
import net.officefloor.eclipse.common.dialog.input.PropertyInputHandler;
import net.officefloor.eclipse.common.dialog.input.ValueTranslator;
import net.officefloor.eclipse.common.dialog.input.ValueTranslatorRegistry;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Popuates the input bean by user entered information via a dialog window.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanDialog extends Dialog {

	/**
	 * Bean to be populated.
	 */
	private final Object bean;

	/**
	 * Properties not to be populated.
	 */
	private final String[] ignoreProperties;

	/**
	 * Types to be ignored.
	 */
	private final List<String> ignoreTypes = new LinkedList<String>();

	/**
	 * {@link ValueTranslatorRegistry}.
	 */
	private final ValueTranslatorRegistry valueTranslatorRegistry;

	/**
	 * Registry of property name to specialised {@link Input}.
	 */
	private final Map<String, Input<?>> specialisedInputs = new HashMap<String, Input<?>>();

	/**
	 * Registry of {@link ValueTranslator} instances by property name.
	 */
	private final Map<String, ValueTranslator> namedTranslators = new HashMap<String, ValueTranslator>();

	/**
	 * Listing of {@link BeanProperty} instances.
	 */
	private List<BeanProperty> beanProperties;

	/**
	 * Flag indicating if the bean was populated.
	 */
	private volatile boolean isPopulated = false;

	/**
	 * Initiate.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param bean
	 *            Bean to be populated.
	 * @param classLoader
	 *            {@link ClassLoader} to use to validate specified classes are
	 *            available.
	 * @param ignoreProperties
	 *            Properties to not populate.
	 */
	public BeanDialog(Shell parentShell, Object bean, ClassLoader classLoader,
			String... ignoreProperties) {
		super(parentShell);
		this.bean = bean;
		this.ignoreProperties = ignoreProperties;
		this.valueTranslatorRegistry = new ValueTranslatorRegistry(classLoader);
	}

	/**
	 * Initiate without the ability to load {@link Class} instances.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param bean
	 *            Bean to be populated.
	 * @param ignoreProperties
	 *            Properties to not populate.
	 */
	public BeanDialog(Shell parentShell, Object bean,
			String... ignoreProperties) {
		this(parentShell, bean, null, ignoreProperties);
	}

	/**
	 * Populates the bean.
	 * 
	 * @return <code>true<code> if bean is populated.
	 */
	public boolean populate() {
		// Block on open
		this.setBlockOnOpen(true);
		this.open();

		// Return whether bean was populated
		return this.isPopulated;
	}

	/**
	 * Adds type to be ignored.
	 * 
	 * @param type
	 *            Type to be ignored. May allow multiple in the one statement.
	 */
	public void addIgnoreType(Class<?> type) {
		this.ignoreTypes.add(type.getName());
	}

	/**
	 * Registers a specialised {@link Input} for the property name.
	 * 
	 * @param propertyName
	 *            Name of property.
	 * @param builder
	 *            Specialised {@link Input}.
	 */
	public void registerPropertyInput(String propertyName, Input<?> input) {
		this.specialisedInputs.put(propertyName, input);

		// Determine if also a value translator for itself
		if (input instanceof ValueTranslator) {
			// Register as value translator
			this.registerPropertyValueTranslator(propertyName,
					(ValueTranslator) input);
		}
	}

	/**
	 * <p>
	 * Registers a {@link ValueTranslator} for the property name.
	 * <p>
	 * Named {@link ValueTranslator} instances override typed
	 * {@link ValueTranslator} instances.
	 * 
	 * @param propertyName
	 *            Name of property.
	 * @param translator
	 *            {@link ValueTranslator}.
	 */
	public void registerPropertyValueTranslator(String propertyName,
			ValueTranslator translator) {
		this.namedTranslators.put(propertyName, translator);
	}

	/**
	 * Obtains the {@link Control} of the dialog area.
	 * 
	 * @return Dialog area.
	 */
	protected Control getDialogArea() {
		return this.dialogArea;
	}

	/*
	 * ======================== Dialog ============================
	 */

	@Override
	protected Control createDialogArea(Composite parent) {

		// Create the set of properties (public mutators)
		Set<String> properties = new HashSet<String>();
		for (Method method : bean.getClass().getMethods()) {
			String methodName = method.getName();

			// Determine if mutator
			if ((Modifier.isPublic(method.getModifiers()))
					&& (Void.TYPE.equals(method.getReturnType()))
					&& (methodName.startsWith("set"))
					&& (method.getParameterTypes().length == 1)) {

				// Determine if ignore type
				Class<?> propertyType = method.getParameterTypes()[0];
				if (this.ignoreTypes.contains(propertyType.getName())) {
					continue; // ignore property
				}

				// Obtain the property name
				String propertyName = this.getPropertyName(method.getName());

				// Add mutator as property
				properties.add(propertyName);
			}
		}

		// Remove any ignore properties
		for (String ignoreProperty : this.ignoreProperties) {
			properties.remove(ignoreProperty);
		}

		// Specify the title of the dialog
		parent.getShell().setText(this.bean.getClass().getSimpleName());

		// Create the composite
		Composite composite = (Composite) super.createDialogArea(parent);

		// Populate the list of properties
		this.beanProperties = new ArrayList<BeanProperty>(properties.size());
		for (String propertyName : properties) {

			// Label the property
			Label label = new Label(composite, SWT.WRAP);
			label.setText(propertyName);
			GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			label.setLayoutData(data);
			label.setFont(parent.getFont());

			// Create the property input handler
			Input<?> input = this.specialisedInputs.get(propertyName);
			if (input == null) {
				input = new DefaultPropertyInput();
			}
			ValueTranslator translator = this.namedTranslators
					.get(propertyName);
			PropertyInputHandler inputHandler = new PropertyInputHandler(
					composite, input, propertyName, this.bean,
					this.valueTranslatorRegistry, translator);

			// Format the control for the property (if not done)
			if (inputHandler.getControl().getLayoutData() == null) {
				inputHandler.getControl().setLayoutData(
						new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			}

			// Position for invalid property notification
			Label errorText = new Label(composite, SWT.WRAP);
			errorText.setText("");
			errorText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
					false));
			errorText.setBackground(errorText.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));
			errorText.setForeground(ColorConstants.red);

			// Create and add the bean property
			BeanProperty beanProperty = new BeanProperty(inputHandler,
					errorText);
			this.beanProperties.add(beanProperty);
		}

		// Return the composite as the control
		return composite;
	}

	@Override
	protected void okPressed() {

		// Ensure all properties are populated
		for (BeanProperty property : this.beanProperties) {

			// Populate the property onto the bean
			try {
				if (!property.inputHandler.populatePropertyOnBean()) {
					// Value must be provided
					property.notifyValueInvalid("Must populate");
					return;
				}
			} catch (InvalidValueException ex) {
				// Must be valid before return
				property.notifyValueInvalid(ex.getMessage());
				return;
			}
		}

		// Properties populated
		this.isPopulated = true;
		super.okPressed();
	}

	/**
	 * Obtains the property name from the input accessor/mutator method name.
	 * 
	 * @param methodName
	 *            Accessor/mutator method name.
	 * @return Property name for the method name.
	 */
	private String getPropertyName(String methodName) {

		// Remove the leading get/set
		methodName = methodName.substring(3);

		// Capitalise first character
		StringBuilder propertyName = new StringBuilder();
		char[] methodChars = methodName.toCharArray();
		propertyName.append(Character.toUpperCase(methodChars[0]));

		// Provide spacing between words of properties (before capitals)
		for (int i = 1; i < methodChars.length; i++) {
			char currentChar = methodChars[i];

			// Add space before a capital
			if (Character.isUpperCase(currentChar)) {
				propertyName.append(' ');
			}

			// Append the character
			propertyName.append(currentChar);
		}

		// Return the property name
		return propertyName.toString();
	}

	/**
	 * Manages property on the bean.
	 */
	private class BeanProperty implements InputListener {

		/**
		 * {@link PropertyInputHandler} for this property.
		 */
		public final PropertyInputHandler inputHandler;

		/**
		 * {@link Label} to report errors for this property.
		 */
		public final Label errorText;

		/**
		 * Initiate.
		 * 
		 * @param inputHandler
		 *            {@link PropertyInputHandler} for this property.
		 * @param errorText
		 *            {@link Label} to report errors for this property.
		 */
		public BeanProperty(PropertyInputHandler inputHandler, Label errorText) {
			this.inputHandler = inputHandler;
			this.errorText = errorText;

			// Specify this as listener
			this.inputHandler.setInputListener(this);
		}

		/*
		 * =================== InputListener ========================
		 */

		@Override
		public void notifyValueChanged(Object value) {
			// Value valid
			this.errorText.setText("");
		}

		@Override
		public void notifyValueInvalid(String message) {
			// Indicate error
			this.errorText.setText(message);
		}
	}

	/**
	 * Default {@link Input} implementation.
	 */
	private class DefaultPropertyInput implements Input<Text> {

		/*
		 * ====================== Input ============================
		 */

		@Override
		public Text buildControl(final InputContext context) {

			// Obtain initial value as string
			Object initialValue = context.getInitialValue();
			String value = "";
			if (initialValue != null) {
				value = initialValue.toString();
			}

			// Defaultly text input
			Composite parent = context.getParent();
			final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
			text.setText(value);
			text.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					// Obtain the value
					String value;
					String current = text.getText();
					if ((e.character == SWT.DEL) || (e.character == '\b')) {
						if (current.length() == 0) {
							value = "";
						} else {
							// Delete the last character
							value = current.substring(0, (current.length() - 1));
						}
					} else {
						value = current + e.text;
					}

					// Provide value to context for validation
					context.notifyValueChanged(value);
				}
			});
			return text;
		}

		@Override
		public String getValue(Text control, InputContext context) {
			// Return the value
			return control.getText();
		}
	}

}