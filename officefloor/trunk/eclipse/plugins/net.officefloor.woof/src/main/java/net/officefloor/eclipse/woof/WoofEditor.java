/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.eclipse.woof;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.DeleteChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputEditPart;
import net.officefloor.eclipse.woof.operations.AddSectionOperation;
import net.officefloor.eclipse.woof.operations.AddTemplateOperation;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofChangesImpl;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofResourceModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputModel;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofEditor extends
		AbstractOfficeFloorEditor<WoofModel, WoofChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.woof";

	/*
	 * ======================== Editor ================================
	 */

	@Override
	protected boolean isDragTarget() {
		return false;
	}

	@Override
	protected WoofChanges createModelChanges(WoofModel model) {
		return new WoofChangesImpl(model);
	}

	@Override
	protected WoofModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new WoofRepositoryImpl(new ModelRepositoryImpl())
				.retrieveWoOF(configuration);
	}

	@Override
	protected void storeModel(WoofModel model, ConfigurationItem configuration)
			throws Exception {
		new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoOF(model,
				configuration);
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(WoofModel.class, WoofEditPart.class);
		map.put(WoofTemplateModel.class, WoofTemplateEditPart.class);
		map.put(WoofTemplateOutputModel.class, WoofTemplateOutputEditPart.class);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the WoOF changes
		WoofChanges woofChanges = this.getModelChanges();

		// Add actions
		list.add(new AddTemplateOperation(woofChanges));
		list.add(new AddSectionOperation(woofChanges));
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting template
		policy.addDelete(WoofTemplateModel.class,
				new DeleteChangeFactory<WoofTemplateModel>() {
					@Override
					public Change<WoofTemplateModel> createChange(
							WoofTemplateModel target) {
						return WoofEditor.this.getModelChanges()
								.removeTemplate(target);
					}
				});

		// Allow deleting section
		policy.addDelete(WoofSectionModel.class,
				new DeleteChangeFactory<WoofSectionModel>() {
					@Override
					public Change<WoofSectionModel> createChange(
							WoofSectionModel target) {
						return WoofEditor.this.getModelChanges().removeSection(
								target);
					}
				});

		// Allow deleting resource
		policy.addDelete(WoofResourceModel.class,
				new DeleteChangeFactory<WoofResourceModel>() {
					@Override
					public Change<WoofResourceModel> createChange(
							WoofResourceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeResource(target);
					}
				});

		// Allow deleting exception
		policy.addDelete(WoofExceptionModel.class,
				new DeleteChangeFactory<WoofExceptionModel>() {
					@Override
					public Change<WoofExceptionModel> createChange(
							WoofExceptionModel target) {
						return WoofEditor.this.getModelChanges()
								.removeException(target);
					}
				});
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect template output to template
		policy.addConnection(
				WoofTemplateOutputModel.class,
				WoofTemplateModel.class,
				new ConnectionChangeFactory<WoofTemplateOutputModel, WoofTemplateModel>() {
					@Override
					public Change<?> createChange(
							WoofTemplateOutputModel source,
							WoofTemplateModel target,
							CreateConnectionRequest request) {
						// TODO implement
						// ConnectionChangeFactory<WoofTemplateOutputModel,WoofTemplateModel>.createChange
						throw new UnsupportedOperationException(
								"TODO implement ConnectionChangeFactory<WoofTemplateOutputModel,WoofTemplateModel>.createChange");
					}
				});
	}

}