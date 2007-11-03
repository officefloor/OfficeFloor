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
package net.officefloor.eclipse.common.dialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.OfficeFloorPluginFailure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
 * @author Daniel
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
	 * Properties of the bean to be populated.
	 */
	private final List<BeanProperty<?>> properties = new LinkedList<BeanProperty<?>>();;

	/**
	 * Registry of property name to specialised {@link PropertyInput}.
	 */
	private final Map<String, PropertyInput<?>> builders = new HashMap<String, PropertyInput<?>>();

	/**
	 * Registry of {@link PropertyTranslator} instances by property name.
	 */
	private final Map<String, PropertyTranslator> namedTranslators = new HashMap<String, PropertyTranslator>();

	/**
	 * Registry of {@link PropertyTranslator} instances by property type.
	 */
	private final Map<Class<?>, PropertyTranslator> typedTranslators = new HashMap<Class<?>, PropertyTranslator>();

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
	 *            {@link java.lang.ClassLoader} to use to validate specified
	 *            classes are available.
	 * @param ignoreProperties
	 *            Properties to not populate.
	 */
	public BeanDialog(Shell parentShell, Object bean,
			final ClassLoader classLoader, String... ignoreProperties) {
		super(parentShell);
		this.bean = bean;
		this.ignoreProperties = ignoreProperties;

		// Register typed translators
		this.typedTranslators.put(String.class, new PropertyTranslator() {
			public Object translate(String value)
					throws InvalidPropertyValueException {
				return value;
			}
		});
		this.typedTranslators.put(Class.class, new PropertyTranslator() {
			public Object translate(String value)
					throws InvalidPropertyValueException {
				try {
					return classLoader.loadClass(value);
				} catch (ClassNotFoundException ex) {
					throw new InvalidPropertyValueException(
							"Can not find class");
				}
			}
		});
		PropertyTranslator intTranslator = new PropertyTranslator() {
			public Object translate(String value)
					throws InvalidPropertyValueException {
				return Integer.parseInt(value);
			}
		};
		this.typedTranslators.put(int.class, intTranslator);
		this.typedTranslators.put(Integer.class, intTranslator);
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
	 * Registers a specialised {@link PropertyInput} for the property name.
	 * 
	 * @param propertyName
	 *            Name of property.
	 * @param builder
	 *            Specialised {@link PropertyInput}.
	 */
	public void registerPropertyInputBuilder(String propertyName,
			PropertyInput<?> builder) {
		this.builders.put(propertyName, builder);
	}

	/**
	 * <p>
	 * Registers a {@link PropertyTranslator} for the property name.
	 * <p>
	 * Named {@link PropertyTranslator} instances override typed
	 * {@link PropertyTranslator} instances.
	 * 
	 * @param propertyName
	 *            Name of property.
	 * @param translator
	 *            {@link PropertyTranslator}.
	 */
	public void registerPropertyTranslator(String propertyName,
			PropertyTranslator translator) {
		this.namedTranslators.put(propertyName, translator);
	}

	/**
	 * Registers a {@link PropertyTranslator} for the property type.
	 * 
	 * @param propertyType
	 *            Type of property.
	 * @param translator
	 *            {@link PropertyTranslator}.
	 */
	public void registerPropertyTranslator(Class<?> propertyType,
			PropertyTranslator translator) {
		this.typedTranslators.put(propertyType, translator);
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
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	protected Control createDialogArea(Composite parent) {

		// Create the map of property accessors and mutators
		Map<String, Method> accessors = new HashMap<String, Method>();
		Map<String, List<Method>> mutatorOptions = new HashMap<String, List<Method>>();
		for (Method method : bean.getClass().getMethods()) {
			String methodName = method.getName();

			// Ignore non-public methods
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			// Determine if accessor
			if ((!Void.TYPE.equals(method.getReturnType()))
					&& (methodName.startsWith("get"))
					&& (method.getParameterTypes().length == 0)) {
				// Accessor (via property name)
				accessors.put(this.getPropertyName(methodName), method);
				continue;
			}

			// Determine if mutator
			if ((Void.TYPE.equals(method.getReturnType()))
					&& (methodName.startsWith("set"))
					&& (method.getParameterTypes().length == 1)) {
				// Mutator (via property name)
				String propertyName = this.getPropertyName(methodName);
				List<Method> propertyMutators = mutatorOptions
						.get(propertyName);
				if (propertyMutators == null) {
					propertyMutators = new LinkedList<Method>();
					mutatorOptions.put(propertyName, propertyMutators);
				}
				propertyMutators.add(method);
				continue;
			}
		}

		// Create the composite
		Composite composite = (Composite) super.createDialogArea(parent);

		// Create the list of properties to populate for the bean
		for (String propertyName : mutatorOptions.keySet()) {

			// Determine if not populate
			boolean isIgnore = false;
			for (String ignoreProperty : this.ignoreProperties) {
				if (propertyName.equals(ignoreProperty)) {
					isIgnore = true;
				}
			}
			if (isIgnore) {
				// Do not add the property
				continue;
			}

			// Add the property
			this.properties.add(this.createBeanProperty(propertyName,
					accessors, mutatorOptions, composite));
		}

		// Populate the list of properties
		for (BeanProperty<?> property : this.properties) {

			// Label the property
			Label label = new Label(composite, SWT.WRAP);
			label.setText(property.name);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());

			// Obtain the control to input the property value
			Control inputControl = property.inputBuilder.buildControl(property);
			inputControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL));

			// Position for invalid property notification
			Label errorText = new Label(composite, SWT.WRAP);
			errorText.setText("");
			errorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL));
			errorText.setBackground(errorText.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));
			errorText.setForeground(ColorConstants.red);

			// Load the bean property with state
			property.state = new PropertyState(inputControl, errorText);
		}

		// Return the composite as the control
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@SuppressWarnings("unchecked")
	protected void okPressed() {

		// Ensure all properties are populated
		for (BeanProperty property : this.properties) {

			// Obtain the value
			String value = property.inputBuilder.getValue(
					property.state.inputControl, property);

			// Ensure the value is not blank
			if ((value == null) || (value.trim().length() == 0)) {
				property.state.flagInvalid("Must populate");
				return;
			}

			// Specify the value on the property
			property.value = value;
		}

		// Ensure all properties are valid
		for (BeanProperty property : this.properties) {
			if (!property.state.isValid) {
				return;
			}
		}

		// Populate the properties on the bean
		for (BeanProperty property : this.properties) {
			try {
				property.populateBean();
			} catch (Exception ex) {
				property.state.flagInvalid(ex.getMessage());
				return;
			}
		}

		// Properties populated, handle ok
		this.isPopulated = true;
		super.okPressed();
	}

	/**
	 * Creates the {@link BeanProperty}.
	 * 
	 * @param propertyName
	 *            Name of the property.
	 * @param accessors
	 *            Accessors of the bean.
	 * @param mutatorOptions
	 *            Mutator options of the bean. The appropriate mutator is
	 *            selected based on the accessor.
	 * @param parent
	 *            Parent {@link Composite}.
	 * @return {@link BeanProperty} for the property.
	 */
	@SuppressWarnings("unchecked")
	private BeanProperty<?> createBeanProperty(String propertyName,
			Map<String, Method> accessors,
			Map<String, List<Method>> mutatorOptions, Composite parent) {

		// Obtain the accessor details
		Class<?> propertyType = null;
		String initialValue = null;
		Method accessor = accessors.get(propertyName);
		if (accessor != null) {

			// Obtain the type of the property
			propertyType = accessor.getReturnType();
			if (Void.TYPE.equals(propertyType)) {
				propertyType = null;
			}

			try {
				// Obtain the initial value
				Object propertyValue = accessor
						.invoke(this.bean, new Object[0]);
				if (propertyValue != null) {
					if (propertyValue instanceof String) {
						initialValue = (String) propertyValue;
					} else {
						initialValue = propertyValue.toString();
					}
				}
			} catch (IllegalArgumentException ex) {
				throw new OfficeFloorPluginFailure(ex);
			} catch (IllegalAccessException ex) {
				throw new OfficeFloorPluginFailure(ex);
			} catch (InvocationTargetException ex) {
				throw new OfficeFloorPluginFailure(ex.getCause());
			}
		}

		// Obtain the mutator details
		Method mutator = null;
		List<Method> mutators = mutatorOptions.get(propertyName);
		if ((mutators == null) || (mutators.size() == 0)) {
			throw new OfficeFloorPluginFailure("No mutator for property '"
					+ propertyName + "' of bean "
					+ this.bean.getClass().getName());
		} else if (mutators.size() == 1) {
			mutator = mutators.get(0);
		} else {
			// Select appropriate mutator (match accessor)
			if (propertyType != null) {
				for (Method method : mutators) {
					if (propertyType.equals(method.getParameterTypes()[0])) {
						mutator = method;
					}
				}
			}

			// No match by accessor then try for String mutator
			if (mutator == null) {
				for (Method method : mutators) {
					if (String.class.equals(method.getParameterTypes()[0])) {
						mutator = method;
					}
				}
			}
		}

		if (mutator != null) {
			// Ensure have the property type
			if (propertyType == null) {
				propertyType = mutator.getParameterTypes()[0];
			}
		} else {
			// Must have mutator
			throw new OfficeFloorPluginFailure(
					"Can not determine mutator for propety '" + propertyName
							+ "' from options");
		}

		// Obtain the property input builder
		PropertyInput<?> inputBuilder = this.builders.get(propertyName);
		if (inputBuilder == null) {
			inputBuilder = new DefaultPropertyInput();
		}

		// Obtain the property transformer (by name then type)
		PropertyTranslator translator = this.namedTranslators.get(propertyName);
		if (translator == null) {
			translator = this.typedTranslators.get(propertyType);
		}
		if (translator == null) {
			// Must have translator
			throw new OfficeFloorPluginFailure(
					"Can not obtain translator for property '"
							+ propertyName
							+ "' of type "
							+ (propertyType == null ? "null" : propertyType
									.getName()));
		}

		// Return the bean property
		return new BeanProperty(propertyName, mutator, inputBuilder,
				translator, parent, initialValue);
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

			// Append the charachter
			propertyName.append(currentChar);
		}

		// Return the property name
		return propertyName.toString();
	}

	/**
	 * Property of the bean.
	 */
	private class BeanProperty<C extends Control> implements
			PropertyInputContext {

		/**
		 * Name of the property.
		 */
		public final String name;

		/**
		 * Mutator to specify the property.
		 */
		public final Method mutator;

		/**
		 * {@link PropertyInput} to build the {@link Control} to input the value
		 * for the property.
		 */
		public final PropertyInput<C> inputBuilder;

		/**
		 * {@link PropertyTranslator} to translate the String value to the
		 * necessary Object value.
		 */
		public final PropertyTranslator translator;

		/**
		 * Parent {@link Composite}.
		 */
		private final Composite parent;

		/**
		 * Attributes.
		 */
		private final Map<String, Object> attributes = new HashMap<String, Object>();

		/**
		 * Initial value.
		 */
		private final Object initialValue;

		/**
		 * Value for the property.
		 */
		public Object value;

		/**
		 * {@link PropertyState} for this property.
		 */
		public PropertyState<C> state = null;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of property.
		 * @param mutator
		 *            Mutator to load the property to the bean.
		 * @param inputBuilder
		 *            {@link PropertyInput} to build the {@link Control} to
		 *            input the value for the property.
		 * @param translator
		 *            {@link PropertyTranslator} to translate the String value
		 *            to the necessary Object value.
		 * @param parent
		 *            Parent {@link Composite}.
		 * @param initialValue
		 *            Initial value.
		 */
		public BeanProperty(String name, Method mutator,
				PropertyInput<C> inputBuilder, PropertyTranslator translator,
				Composite parent, Object initialValue) {
			this.name = name;
			this.mutator = mutator;
			this.inputBuilder = inputBuilder;
			this.translator = translator;
			this.parent = parent;
			this.initialValue = initialValue;
			this.value = initialValue;
		}

		/**
		 * Populates the bean with the property.
		 */
		public void populateBean() throws Exception {
			this.mutator.invoke(BeanDialog.this.bean,
					new Object[] { this.value });
		}

		/*
		 * ========================================================================
		 * VerifyListener
		 * ========================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.PropertyInputContext#getInitialValue()
		 */
		@Override
		public Object getInitialValue() {
			return this.initialValue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.PropertyInputContext#getParent()
		 */
		@Override
		public Composite getParent() {
			return this.parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.PropertyInputContext#notifyValueChanged(java.lang.String)
		 */
		@Override
		public void notifyValueChanged(String value) {
			try {
				// Obtain the property String value
				String stringValue = this.inputBuilder.getValue(
						this.state.inputControl, this);

				// Attempt to transform the value
				this.value = this.translator.translate(stringValue);

				// Value is valid
				this.state.flagValid();

			} catch (InvalidPropertyValueException ex) {
				// Value is invalid
				this.state.flagInvalid(ex.getMessage());
			} catch (NumberFormatException ex) {
				// Invalid number
				this.state.flagInvalid("Invalid number");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.PropertyInputContext#getAttribute(java.lang.String)
		 */
		@Override
		public Object getAttribute(String name) {
			return this.attributes.get(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.PropertyInputContext#setAttribute(java.lang.String,
		 *      java.lang.Object)
		 */
		@Override
		public void setAttribute(String name, Object value) {
			this.attributes.put(name, value);
		}
	}

	/**
	 * Maintains state of the property.
	 */
	private class PropertyState<C extends Control> {

		/**
		 * Defaultly valid.
		 */
		public boolean isValid = true;

		/**
		 * Input {@link Control}.
		 */
		public final C inputControl;

		/**
		 * {@link Label} to report errors.
		 */
		private final Label errorText;

		/**
		 * Initiate.
		 * 
		 * @param inputControl
		 *            Input {@link Control}.
		 * @param errorText
		 *            {@link Label} to report errors.
		 */
		public PropertyState(C inputControl, Label errorText) {
			this.inputControl = inputControl;
			this.errorText = errorText;
		}

		/**
		 * Specifies the property is valid.
		 */
		public void flagValid() {
			this.isValid = true;
			this.errorText.setText("");
		}

		/**
		 * Specifies an error for the property.
		 * 
		 * @param message
		 *            Error message.
		 */
		public void flagInvalid(String message) {
			this.isValid = false;
			this.errorText.setText(message);
		}
	}
}

/**
 * Default {@link net.officefloor.eclipse.common.dialog.PropertyInput}
 * implementation.
 */
class DefaultPropertyInput implements PropertyInput<Text> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.PropertyInput#buildControl(net.officefloor.eclipse.common.dialog.PropertyInputContext)
	 */
	@Override
	public Text buildControl(final PropertyInputContext context) {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.PropertyInput#getValue(org.eclipse.swt.widgets.Control,
	 *      net.officefloor.eclipse.common.dialog.PropertyInputContext)
	 */
	@Override
	public String getValue(Text control, PropertyInputContext context) {
		// Return the value
		return control.getText();
	}
}
