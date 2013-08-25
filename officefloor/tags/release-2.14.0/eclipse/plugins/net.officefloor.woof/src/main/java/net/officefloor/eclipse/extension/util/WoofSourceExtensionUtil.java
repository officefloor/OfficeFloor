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
import net.officefloor.eclipse.extension.template.WoofTemplateExtensionSourceExtensionContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class providing helper methods for WoOF source extension
 * implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSourceExtensionUtil extends SourceExtensionUtil {

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
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyClass(String label, String name,
			Composite container,
			WoofTemplateExtensionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyClass(label, name, container,
				new WoofTemplateExtensionGeneric(context), listener);
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
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyResource(String label, String name,
			Composite container,
			WoofTemplateExtensionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyResource(label, name, container,
				new WoofTemplateExtensionGeneric(context), listener);
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
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link Text}.
	 */
	public static Property createPropertyText(String label, String name,
			String defaultValue, Composite container,
			final WoofTemplateExtensionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyText(label, name, defaultValue, container,
				new WoofTemplateExtensionGeneric(context), listener);
	}

	/**
	 * Creates the display for the input {@link PropertyList}.
	 * 
	 * @param label
	 *            Label for the {@link Property}.
	 * @param container
	 *            {@link Composite} to add display {@link Control} instances.
	 * @param context
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param hidePropertyNames
	 *            {@link Property} names to hide.
	 */
	public static void createPropertyList(String label, Composite container,
			final WoofTemplateExtensionSourceExtensionContext context,
			String... hidePropertyNames) {
		createPropertyList(label, container, new WoofTemplateExtensionGeneric(
				context), hidePropertyNames);
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
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCheckbox(String label, String name,
			boolean defaultValue, String trueValue, String falseValue,
			Composite container,
			WoofTemplateExtensionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCheckbox(label, name, defaultValue, trueValue,
				falseValue, container,
				new WoofTemplateExtensionGeneric(context), listener);
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
	 *            {@link WoofTemplateExtensionSourceExtensionContext}.
	 * @param listener
	 *            {@link PropertyValueChangeListener}. May be <code>null</code>.
	 * @return {@link Property} for the {@link SWT#CHECK} {@link Button}.
	 */
	public static Property createPropertyCombo(String label, String name,
			String defaultValue, String[] values, Composite container,
			WoofTemplateExtensionSourceExtensionContext context,
			PropertyValueChangeListener listener) {
		return createPropertyCombo(label, name, defaultValue, values,
				container, new WoofTemplateExtensionGeneric(context), listener);
	}

	/**
	 * {@link WoofTemplateExtensionSource} {@link GenericSourceExtensionContext}
	 * .
	 */
	private static class WoofTemplateExtensionGeneric implements
			GenericSourceExtensionContext {

		/**
		 * {@link WoofTemplateExtensionSourceExtensionContext}.
		 */
		private final WoofTemplateExtensionSourceExtensionContext context;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link WoofTemplateExtensionSourceExtensionContext}.
		 */
		public WoofTemplateExtensionGeneric(
				WoofTemplateExtensionSourceExtensionContext context) {
			this.context = context;
		}

		/*
		 * =============== GenericSourceExtensionContext ===============
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