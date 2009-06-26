/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.DeleteChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.section.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionInputEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputToExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputToSubSectionInputEditPart;
import net.officefloor.eclipse.section.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.section.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.section.operations.AddSubSectionOperation;
import net.officefloor.eclipse.section.operations.ToggleSubSectionInputPublicOperation;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditor extends
		AbstractOfficeFloorEditor<SectionModel, SectionChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.section";

	@Override
	protected boolean isDragTarget() {
		// Disallow as drag target
		return false;
	}

	@Override
	protected SectionModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new SectionRepositoryImpl(new ModelRepositoryImpl())
				.retrieveSection(configuration);
	}

	@Override
	protected void storeModel(SectionModel model,
			ConfigurationItem configuration) throws Exception {
		new SectionRepositoryImpl(new ModelRepositoryImpl()).storeSection(
				model, configuration);
	}

	@Override
	protected SectionChanges createModelChanges(SectionModel model) {
		return new SectionChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {
		// Entities
		map.put(SectionModel.class, SectionEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);
		map.put(SubSectionModel.class, SubSectionEditPart.class);
		map.put(SubSectionInputModel.class, SubSectionInputEditPart.class);
		map.put(SubSectionOutputModel.class, SubSectionOutputEditPart.class);
		map.put(SubSectionObjectModel.class, SubSectionObjectEditPart.class);

		// Connections
		map.put(SubSectionObjectToExternalManagedObjectModel.class,
				SubSectionObjectToExternalManagedObjectEditPart.class);
		map.put(SubSectionOutputToSubSectionInputModel.class,
				SubSectionOutputToSubSectionInputEditPart.class);
		map.put(SubSectionOutputToExternalFlowModel.class,
				SubSectionOutputToExternalFlowEditPart.class);
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting sub section
		policy.addDelete(SubSectionModel.class,
				new DeleteChangeFactory<SubSectionModel>() {
					@Override
					public Change<SubSectionModel> createChange(
							SubSectionModel target) {
						return SectionEditor.this.getModelChanges()
								.removeSubSection(target);
					}
				});

		// Allow deleting external flow
		policy.addDelete(ExternalFlowModel.class,
				new DeleteChangeFactory<ExternalFlowModel>() {
					@Override
					public Change<ExternalFlowModel> createChange(
							ExternalFlowModel target) {
						return SectionEditor.this.getModelChanges()
								.removeExternalFlow(target);
					}
				});

		// Allow deleting external managed object
		policy.addDelete(ExternalManagedObjectModel.class,
				new DeleteChangeFactory<ExternalManagedObjectModel>() {
					@Override
					public Change<ExternalManagedObjectModel> createChange(
							ExternalManagedObjectModel target) {
						return SectionEditor.this.getModelChanges()
								.removeExternalManagedObject(target);
					}
				});

		// Allow deleting sub section object to external managed object
		policy
				.addDelete(
						SubSectionObjectToExternalManagedObjectModel.class,
						new DeleteChangeFactory<SubSectionObjectToExternalManagedObjectModel>() {
							@Override
							public Change<SubSectionObjectToExternalManagedObjectModel> createChange(
									SubSectionObjectToExternalManagedObjectModel target) {
								return SectionEditor.this
										.getModelChanges()
										.removeSubSectionObjectToExternalManagedObject(
												target);
							}
						});

		// Allow deleting sub section output to sub section input
		policy
				.addDelete(
						SubSectionOutputToSubSectionInputModel.class,
						new DeleteChangeFactory<SubSectionOutputToSubSectionInputModel>() {
							@Override
							public Change<SubSectionOutputToSubSectionInputModel> createChange(
									SubSectionOutputToSubSectionInputModel target) {
								return SectionEditor.this
										.getModelChanges()
										.removeSubSectionOutputToSubSectionInput(
												target);
							}
						});

		// Allow deleting sub section output to external flow
		policy.addDelete(SubSectionOutputToExternalFlowModel.class,
				new DeleteChangeFactory<SubSectionOutputToExternalFlowModel>() {
					@Override
					public Change<SubSectionOutputToExternalFlowModel> createChange(
							SubSectionOutputToExternalFlowModel target) {
						return SectionEditor.this.getModelChanges()
								.removeSubSectionOutputToExternalFlow(target);
					}
				});
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect sub section object to external managed object
		policy
				.addConnection(
						SubSectionObjectModel.class,
						ExternalManagedObjectModel.class,
						new ConnectionChangeFactory<SubSectionObjectModel, ExternalManagedObjectModel>() {
							@Override
							public Change<?> createChange(
									SubSectionObjectModel source,
									ExternalManagedObjectModel target,
									CreateConnectionRequest request) {
								return SectionEditor.this
										.getModelChanges()
										.linkSubSectionObjectToExternalManagedObject(
												source, target);
							}
						});

		// Connect sub section output to sub section input
		policy
				.addConnection(
						SubSectionOutputModel.class,
						SubSectionInputModel.class,
						new ConnectionChangeFactory<SubSectionOutputModel, SubSectionInputModel>() {
							@Override
							public Change<?> createChange(
									SubSectionOutputModel source,
									SubSectionInputModel target,
									CreateConnectionRequest request) {
								return SectionEditor.this.getModelChanges()
										.linkSubSectionOutputToSubSectionInput(
												source, target);
							}
						});

		// Connect sub section output to external flow
		policy
				.addConnection(
						SubSectionOutputModel.class,
						ExternalFlowModel.class,
						new ConnectionChangeFactory<SubSectionOutputModel, ExternalFlowModel>() {
							@Override
							public Change<?> createChange(
									SubSectionOutputModel source,
									ExternalFlowModel target,
									CreateConnectionRequest request) {
								return SectionEditor.this.getModelChanges()
										.linkSubSectionOutputToExternalFlow(
												source, target);
							}
						});
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the section changes
		SectionChanges sectionChanges = this.getModelChanges();

		// Add operations
		list.add(new AddSubSectionOperation(sectionChanges));
		list.add(new AddExternalManagedObjectOperation(sectionChanges));
		list.add(new AddExternalFlowOperation(sectionChanges));

		// Sub room input flow operations
		list.add(new ToggleSubSectionInputPublicOperation(sectionChanges));
	}

}