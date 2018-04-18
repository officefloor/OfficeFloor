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
package net.officefloor.eclipse.section;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.operations.ITransactionalOperation;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.configurer.CloseListener;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedErrorHandler;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
import net.officefloor.eclipse.editor.ChangeExecutor;
import net.officefloor.eclipse.editor.DefaultImages;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.eclipse.osgi.ProjectConfigurationContext;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalFlowModel.ExternalFlowEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

/**
 * {@link OfficeFloorModel} editor.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditor extends AbstractFXEditor {

	/**
	 * {@link Injector}.
	 */
	private final Injector injector;

	/**
	 * {@link AdaptedEditorModule}.
	 */
	private AdaptedEditorModule module;

	/**
	 * {@link AdaptedErrorHandler}.
	 */
	private AdaptedErrorHandler errorHandler;

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private WritableConfigurationItem configurationItem;

	/**
	 * {@link Model} being edited.
	 */
	private SectionModel model;

	/**
	 * {@link OfficeFloorOsgiBridge}.
	 */
	private OfficeFloorOsgiBridge osgiBridge;

	/**
	 * Instantiate to capture {@link Injector}.
	 * 
	 * @param injector
	 *            {@link Injector}.
	 */
	private SectionEditor(Injector injector) {
		super(injector);
		this.injector = injector;
	}

	/**
	 * Instantiate to capture {@link AdaptedEditorModule}.
	 * 
	 * @param module
	 *            {@link AdaptedEditorModule}.
	 */
	private SectionEditor(AdaptedEditorModule module) {
		this(Guice.createInjector(Modules.override(module).with(new MvcFxUiModule())));
		this.module = module;
	}

	/**
	 * Instantiate.
	 * 
	 * @param adaptedBuilder
	 *            {@link AdaptedBuilder}.
	 */
	public SectionEditor() {
		this(new AdaptedEditorModule());

		// Initialise
		this.module.initialise(this.getDomain(), this.injector);
	}

	/**
	 * Obtains the {@link AdaptedBuilder}.
	 * 
	 * @return {@link AdaptedBuilder}.
	 */
	protected AdaptedBuilder getAdaptedBuilder() {
		return (context) -> {

			AdaptedRootBuilder<SectionModel, SectionChanges> root = context.root(SectionModel.class,
					(m) -> new SectionChangesImpl(m));

			// Obtain the error handler
			this.errorHandler = root.getErrorHandler();

			// External Flow
			AdaptedParentBuilder<SectionModel, SectionChanges, ExternalFlowModel, ExternalFlowEvent> externalFlow = root
					.parent(new ExternalFlowModel("External Flow", null), (m) -> m.getExternalFlows(),
							(parent, ctx) -> {
								HBox container = new HBox();
								ctx.label(container);
								return container;
							}, SectionEvent.ADD_EXTERNAL_FLOW, SectionEvent.REMOVE_EXTERNAL_FLOW);
			externalFlow.label((m) -> m.getExternalFlowName(), ExternalFlowEvent.CHANGE_EXTERNAL_FLOW_NAME);
			externalFlow.create((ctx) -> {

				// Obtain details for dialog
				OfficeFloorOsgiBridge bridge = this.getOsgiBridge();
				Shell shell = this.getEditorSite().getShell();

				// Obtain details for executing change
				SectionChanges changes = ctx.getOperations();
				ChangeExecutor executor = ctx.getChangeExecutor();

				// Create the overlay to add external flow
				ctx.overlay((visual) -> {

					// Prepare the parent
					Pane parent = visual.getOverlayParent();
					parent.setPrefHeight(200);
					parent.applyCss();

					// Create the configurer
					Configurer<ExternalFlowConfiguration> configurer = new Configurer<>(bridge, shell);
					ExternalFlowConfiguration configuration = new ExternalFlowConfiguration();

					// Load configuration
					configuration.loadAddConfiguration(configurer, changes, new ChangeExecutor() {

						@Override
						public void execute(ITransactionalOperation operation) {
							executor.execute(operation);
						}

						@Override
						public void execute(Change<?> change) {

							// Position the change
							ctx.position((ExternalFlowModel) change.getTarget());

							// Undertake the change
							executor.execute(change);
						}
					});

					((ConfigurationBuilder<ExternalFlowConfiguration>) configurer).close(new CloseListener() {

						@Override
						public void cancelled() {
							visual.close();
						}

						@Override
						public void applied() {
							visual.close();
						}
					});

					// Show the configuration
					configurer.loadConfiguration(configuration, parent);
				});
			});
			externalFlow.action((ctx) -> {
				ctx.getChangeExecutor().execute(ctx.getOperations().removeExternalFlow(ctx.getModel()));
			}, DefaultImages.DELETE);

		};
	}

	/**
	 * Obtains the {@link OfficeFloorOsgiBridge}.
	 * 
	 * @return {@link OfficeFloorOsgiBridge}.
	 * @throws Exception
	 *             If fails to obtain the {@link OfficeFloorOsgiBridge}.
	 */
	protected OfficeFloorOsgiBridge getOsgiBridge() throws Exception {

		// Determine if cached access to compiler
		if (this.osgiBridge != null) {
			return this.osgiBridge;
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
		this.osgiBridge = new OfficeFloorOsgiBridge(javaProject);

		// Obtain the OfficeFloor compiler
		return this.osgiBridge;
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
		Pane view = this.module.createParent(this.getAdaptedBuilder());

		// Create scene and populate canvas with view
		this.getCanvas().setScene(new Scene(view));
	}

	@Override
	protected void activate() {

		// Load the model
		this.errorHandler.isError(() -> {
			SectionModel section = new SectionModel();
			new SectionRepositoryImpl(new ModelRepositoryImpl()).retrieveSection(section, this.configurationItem);
			this.model = section;
		});

		// Load the module
		this.module.loadRootModel(this.model);

		// Activate
		super.activate();
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Input changed, so reset for new input
		this.osgiBridge = null;

		// Obtain the input configuration
		IFileEditorInput fileInput = (IFileEditorInput) input;
		IFile file = fileInput.getFile();
		this.configurationItem = ProjectConfigurationContext.getWritableConfigurationItem(file, null);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		this.errorHandler.isError(() -> {

			// Save the model
			new SectionRepositoryImpl(new ModelRepositoryImpl()).storeSection(this.model, this.configurationItem);

			// Flag saved (no longer dirty)
			this.markNonDirty();
		});
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
		// Not able to save as
	}

}