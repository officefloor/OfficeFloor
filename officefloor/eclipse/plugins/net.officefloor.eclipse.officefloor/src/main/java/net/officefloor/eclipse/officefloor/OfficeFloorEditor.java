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
package net.officefloor.eclipse.officefloor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.eclipse.configurer.dialog.ConfigurerDialog;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.javaproject.JavaProjectOfficeFloorCompiler;
import net.officefloor.model.impl.officefloor.OfficeFloorChangesImpl;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeModel.DeployedOfficeEvent;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorModel.OfficeFloorEvent;

/**
 * {@link OfficeFloorModel} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditor extends AbstractFXEditor {

	/**
	 * {@link Injector}.
	 */
	private final Injector injector;

	/**
	 * {@link AdaptedEditorModule}.
	 */
	private AdaptedEditorModule module;

	/**
	 * {@link JavaProjectOfficeFloorCompiler}.
	 */
	private JavaProjectOfficeFloorCompiler javaProjectOfficeFloorCompiler;

	/**
	 * Instantiate to capture {@link Injector}.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 */
	private OfficeFloorEditor(Injector injector) {
		super(injector);
		this.injector = injector;
	}

	/**
	 * Instantiate to capture {@link AdaptedEditorModule}.
	 * 
	 * @param module
	 *            {@link AdaptedEditorModule}.
	 */
	private OfficeFloorEditor(AdaptedEditorModule module) {
		this(Guice.createInjector(Modules.override(module).with(new MvcFxUiModule())));
		this.module = module;
	}

	/**
	 * Instantiate.
	 * 
	 * @param adaptedBuilder
	 *            {@link AdaptedBuilder}.
	 */
	public OfficeFloorEditor() {
		this(new AdaptedEditorModule());

		// Initialise the module
		this.module.initialise(this.getDomain(), this.injector, this.getAdaptedBuilder());
	}

	/**
	 * Obtains the {@link AdaptedBuilder}.
	 * 
	 * @return {@link AdaptedBuilder}.
	 */
	protected AdaptedBuilder getAdaptedBuilder() {
		return (context) -> {

			AdaptedRootBuilder<OfficeFloorModel, OfficeFloorChanges> root = context.root(OfficeFloorModel.class,
					(m) -> new OfficeFloorChangesImpl(m));

			// Configure the Deployed Office
			AdaptedParentBuilder<OfficeFloorModel, OfficeFloorChanges, DeployedOfficeModel, DeployedOfficeEvent> office = root
					.parent(new DeployedOfficeModel("Office", null, null), (m) -> m.getDeployedOffices(), (m, ctx) -> {
						VBox visual = new VBox();
						ctx.label(visual);
						return visual;
					}, OfficeFloorEvent.ADD_DEPLOYED_OFFICE, OfficeFloorEvent.REMOVE_DEPLOYED_OFFICE);
			office.create((ctx) -> {
				try {
					
					Shell shell = this.getEditorSite().getShell();

					ConfigurerDialog<DeployedOfficeConfiguration> dialog = new ConfigurerDialog<>("Add",
							"Add " + DeployedOfficeModel.class.getSimpleName());
					DeployedOfficeConfiguration configuration = new DeployedOfficeConfiguration();
					configuration.loadAddConfiguration(dialog, ctx, shell, this.getJavaProjectOfficeFloorCompiler());
					dialog.configureModel(configuration);

				} catch (Exception ex) {
					
					// TODO allow error to be shown
					System.err.println("FAILED TO CREATE");
					ex.printStackTrace();
				}
			});
			office.label((m) -> m.getDeployedOfficeName(), DeployedOfficeEvent.CHANGE_DEPLOYED_OFFICE_NAME);

		};
	}

	/**
	 * Obtains the {@link JavaProjectOfficeFloorCompiler}.
	 * 
	 * @return {@link JavaProjectOfficeFloorCompiler}.
	 * @throws Exception
	 *             If fails to obtain the {@link JavaProjectOfficeFloorCompiler}.
	 */
	protected JavaProjectOfficeFloorCompiler getJavaProjectOfficeFloorCompiler() throws Exception {

		// Determine if cached access to compiler
		if (this.javaProjectOfficeFloorCompiler != null) {
			return this.javaProjectOfficeFloorCompiler;
		}

		// Obtain the file input
		IEditorInput input = this.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			throw new Exception(
					"Invalid IEditorInput as expecting a file (" + (input == null ? null : input.getClass().getName()));
		}
		IFileEditorInput fileInput = (IFileEditorInput) input;

		// Obtain the file (and subsequently it's project)
		IFile file = fileInput.getFile();
		IProject project = file.getProject();

		// Obtain the java project
		IJavaProject javaProject = JavaCore.create(project);

		// Bridge java project to OfficeFloor compiler
		this.javaProjectOfficeFloorCompiler = new JavaProjectOfficeFloorCompiler(javaProject);

		// Obtain the OfficeFloor compiler
		return this.javaProjectOfficeFloorCompiler;
	}

	/*
	 * ============== AbstractFXEditor ==================
	 */

	@Override
	public IViewer getContentViewer() {
		return this.module.getContentViewer();
	}

	@Override
	protected void hookViewers() {

		// Create the view
		Pane view = this.module.createParent(null);

		// Create scene and populate canvas with view
		this.getCanvas().setScene(new Scene(view));
	}

	@Override
	protected void activate() {
		super.activate();

		// Load the module
		this.module.loadRootModel(new OfficeFloorModel());
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Input changed, so reset for new input
		this.javaProjectOfficeFloorCompiler = null;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

}