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
package net.officefloor.eclipse.configurer.test;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.model.Model;

/**
 * Example configurer view.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerView {

	@Inject
	public ExampleConfigurerView(Composite parent) throws Exception {

		// Obtain the java project
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Test");
		IJavaProject javaProject = JavaCore.create(project);

		// Obtain the shell
		Shell shell = parent.getShell();

		// Create the configurer
		Configurer<ExampleModel> configurer = new Configurer<>();

		// Provide configuration
		ConfigurationBuilder<ExampleModel> builder = configurer;
		builder.text("test").init((model) -> model.text).setValue((model, value) -> model.text = value);
		builder.flag("flag").init((model) -> model.flag).setValue((model, value) -> model.flag = value);
		builder.clazz("Class", javaProject, shell).init((model) -> model.className);
		builder.clazz("Model Class", javaProject, shell).init((model) -> model.modelClassName)
				.setValue((model, value) -> model.modelClassName = value).superType(Model.class);
		builder.clazz("Missing class", javaProject, shell).superType(ExampleConfigurerView.class);

		// Load model to view
		configurer.loadConfiguration(new ExampleModel(), parent);
	}
}