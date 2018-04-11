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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.mvc.fx.ui.MvcFxUiModule;
import org.eclipse.gef.mvc.fx.ui.parts.AbstractFXEditor;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.editor.AdaptedBuilder;
import net.officefloor.eclipse.editor.AdaptedEditorModule;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.AdaptedRootBuilder;
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

		// Initialise the module
		this.module.initialise(this.getDomain(), this.injector);
	}

	/**
	 * Instantiate.
	 * 
	 * @param adaptedBuilder
	 *            {@link AdaptedBuilder}.
	 */
	protected OfficeFloorEditor(AdaptedBuilder adaptedBuilder) {
		this(new AdaptedEditorModule(adaptedBuilder));
	}

	/**
	 * Instantiate.
	 */
	public OfficeFloorEditor() {
		this((context) -> {

			AdaptedRootBuilder<OfficeFloorModel, OfficeFloorChanges> root = context.root(OfficeFloorModel.class,
					(m) -> new OfficeFloorChangesImpl(m));

			// Configure the Deployed Office
			AdaptedParentBuilder<OfficeFloorModel, OfficeFloorChanges, DeployedOfficeModel, DeployedOfficeEvent> office = root
					.parent(new DeployedOfficeModel("Office", null, null), (m) -> m.getDeployedOffices(), (m, ctx) -> {
						VBox visual = new VBox();
						ctx.label(visual);
						return visual;
					}, OfficeFloorEvent.ADD_DEPLOYED_OFFICE, OfficeFloorEvent.REMOVE_DEPLOYED_OFFICE);
			office.create((ctx) -> ctx.execute(ctx.getOperations().addDeployedOffice("Office",
					"net.example.OfficeFloor", "location", null, null)));
			office.label((m) -> m.getDeployedOfficeName(), DeployedOfficeEvent.CHANGE_DEPLOYED_OFFICE_NAME);

		});
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
		Pane view = this.module.createParent();

		// Create scene and populate canvas with view
		this.getCanvas().setScene(new Scene(view));
	}

	@Override
	protected void activate() {
		super.activate();

		this.module.loadRootModel(new OfficeFloorModel());
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