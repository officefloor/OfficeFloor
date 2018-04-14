/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.dialog;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.collections.ObservableList;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.ErrorListener;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.model.Model;

/**
 * Dialog for a {@link Configurer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurerDialog<M> implements ConfigurationBuilder<M> {

	/**
	 * {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * {@link Configurer}.
	 */
	private final Configurer<M> configurer = new Configurer<>();

	/**
	 * Instantiate.
	 * 
	 * @param parentShell
	 *            {@link Shell}.
	 */
	public ConfigurerDialog(Shell parentShell) {
		this.parentShell = parentShell;
	}

	/**
	 * Opens the dialog to configure the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 */
	public void open(M model) {

		// Create dialog shell
		Shell dialog = new Shell(this.parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setLayout(new RowLayout());

		// Load configuration
		this.configurer.loadConfiguration(model, dialog);

		// Display dialog
		dialog.pack();
		dialog.open();
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	/*
	 * =============== ConfigurationBuilder ====================
	 */

	@Override
	public void title(String title) {
		this.configurer.title(title);
	}

	@Override
	public ChoiceBuilder<M> choices(String label) {
		return this.configurer.choices(label);
	}

	@Override
	public <I> ListBuilder<M, I> list(String label, Class<I> itemType) {
		return this.configurer.list(label, itemType);
	}

	@Override
	public <I> MultipleBuilder<M, I> multiple(String label, Class<I> itemType) {
		return this.configurer.multiple(label, itemType);
	}

	@Override
	public PropertiesBuilder<M> properties(String label) {
		return this.configurer.properties(label);
	}

	@Override
	public MappingBuilder<M> map(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets) {
		return this.configurer.map(label, getSources, getTargets);
	}

	@Override
	public ClassBuilder<M> clazz(String label, IJavaProject javaProject, Shell shell) {
		return this.configurer.clazz(label, javaProject, shell);
	}

	@Override
	public ResourceBuilder<M> resource(String label, IJavaProject javaProject, Shell shell) {
		return this.configurer.resource(label, javaProject, shell);
	}

	@Override
	public TextBuilder<M> text(String label) {
		return this.configurer.text(label);
	}

	@Override
	public FlagBuilder<M> flag(String label) {
		return this.configurer.flag(label);
	}

	@Override
	public void validate(ValueValidator<M> validator) {
		this.configurer.validate(validator);
	}

	@Override
	public void error(ErrorListener errorListener) {
		this.configurer.error(errorListener);
	}

	@Override
	public void apply(String label, Consumer<M> applier) {
		this.configurer.apply(label, applier);
	}

}