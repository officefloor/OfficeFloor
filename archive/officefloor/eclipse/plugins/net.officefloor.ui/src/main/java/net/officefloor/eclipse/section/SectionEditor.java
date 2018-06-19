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
package net.officefloor.eclipse.section;

import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.ui.IEditorPart;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.section.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.FunctionEditPart;
import net.officefloor.eclipse.section.editparts.FunctionEscalationEditPart;
import net.officefloor.eclipse.section.editparts.FunctionEscalationToExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.FunctionEscalationToFunctionEditPart;
import net.officefloor.eclipse.section.editparts.FunctionFlowEditPart;
import net.officefloor.eclipse.section.editparts.FunctionFlowToExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.FunctionFlowToFunctionEditPart;
import net.officefloor.eclipse.section.editparts.FunctionNamespaceEditPart;
import net.officefloor.eclipse.section.editparts.FunctionToNextExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.FunctionToNextFunctionEditPart;
import net.officefloor.eclipse.section.editparts.ManagedFunctionEditPart;
import net.officefloor.eclipse.section.editparts.ManagedFunctionObjectEditPart;
import net.officefloor.eclipse.section.editparts.ManagedFunctionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.ManagedFunctionObjectToSectionManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.ManagedFunctionToFunctionEditPart;
import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectDependencyEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectDependencyToExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectDependencyToSectionManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceFlowEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceFlowToExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceFlowToFunctionEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceFlowToSubSectionInputEditPart;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectToSectionManagedObjectSourceEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionInputEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectToSectionManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputToExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputToSubSectionInputEditPart;
import net.officefloor.eclipse.section.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.section.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.section.operations.AddFunctionNamespaceOperation;
import net.officefloor.eclipse.section.operations.AddManagedObjectOperation;
import net.officefloor.eclipse.section.operations.AddManagedObjectSourceOperation;
import net.officefloor.eclipse.section.operations.AddSubSectionOperation;
import net.officefloor.eclipse.section.operations.CreateFunctionFromManagedFunctionOperation;
import net.officefloor.eclipse.section.operations.DeleteExternalFlowOperation;
import net.officefloor.eclipse.section.operations.DeleteExternalManagedObjectOperation;
import net.officefloor.eclipse.section.operations.DeleteFunctionNamespaceOperation;
import net.officefloor.eclipse.section.operations.DeleteFunctionOperation;
import net.officefloor.eclipse.section.operations.DeleteManagedObjectOperation;
import net.officefloor.eclipse.section.operations.DeleteManagedObjectSourceOperation;
import net.officefloor.eclipse.section.operations.DeleteSubSectionOperation;
import net.officefloor.eclipse.section.operations.RefactorFunctionNamespaceOperation;
import net.officefloor.eclipse.section.operations.ToggleFunctionPublicOperation;
import net.officefloor.eclipse.section.operations.ToggleManagedFunctionObjectParameterOperation;
import net.officefloor.eclipse.section.operations.ToggleSubSectionInputPublicOperation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionChangesImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * Editor for the {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionEditor extends AbstractOfficeFloorEditor<SectionModel, SectionChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.section";

	@Override
	protected SectionModel retrieveModel(ConfigurationItem configuration) throws Exception {
		SectionModel section = new SectionModel();
		new SectionRepositoryImpl(new ModelRepositoryImpl()).retrieveSection(section, configuration);
		return section;
	}

	/**
	 * Obtains the whether spawns {@link ThreadState}.
	 * 
	 * @param instigationStrategy
	 *            Instigation type.
	 * @return {@link FlowInstigationStrategyEnum} or <code>null</code> if
	 *         unknown instigation strategy.
	 */
	public boolean isSpawnThreadState(Object spawnStrategy) {

		// Ensure indication of whether spawning thread state
		if (spawnStrategy == null) {
			this.messageError("Must specify spawning of thread state");
			return false;
		}

		// Obtain whether spawning thread state
		return Boolean.parseBoolean(spawnStrategy.toString());
	}

	/*
	 * ===================== AbstractOfficeFloorEditor ====================
	 */

	@Override
	protected void storeModel(SectionModel model, WritableConfigurationItem configuration) throws Exception {
		new SectionRepositoryImpl(new ModelRepositoryImpl()).storeSection(model, configuration);
	}

	@Override
	protected SectionChanges createModelChanges(SectionModel model) {
		return new SectionChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(SectionModel.class, SectionEditPart.class);
		map.put(ExternalManagedObjectModel.class, ExternalManagedObjectEditPart.class);
		map.put(SectionManagedObjectSourceModel.class, SectionManagedObjectSourceEditPart.class);
		map.put(SectionManagedObjectSourceFlowModel.class, SectionManagedObjectSourceFlowEditPart.class);
		map.put(SectionManagedObjectModel.class, SectionManagedObjectEditPart.class);
		map.put(SectionManagedObjectDependencyModel.class, SectionManagedObjectDependencyEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);
		map.put(SubSectionModel.class, SubSectionEditPart.class);
		map.put(SubSectionInputModel.class, SubSectionInputEditPart.class);
		map.put(SubSectionOutputModel.class, SubSectionOutputEditPart.class);
		map.put(SubSectionObjectModel.class, SubSectionObjectEditPart.class);
		map.put(FunctionNamespaceModel.class, FunctionNamespaceEditPart.class);
		map.put(ManagedFunctionModel.class, ManagedFunctionEditPart.class);
		map.put(ManagedFunctionObjectModel.class, ManagedFunctionObjectEditPart.class);
		map.put(FunctionModel.class, FunctionEditPart.class);
		map.put(FunctionFlowModel.class, FunctionFlowEditPart.class);
		map.put(FunctionEscalationModel.class, FunctionEscalationEditPart.class);

		// Connections
		map.put(SectionManagedObjectSourceFlowToSubSectionInputModel.class,
				SectionManagedObjectSourceFlowToSubSectionInputEditPart.class);
		map.put(SectionManagedObjectSourceFlowToExternalFlowModel.class,
				SectionManagedObjectSourceFlowToExternalFlowEditPart.class);
		map.put(SectionManagedObjectSourceFlowToFunctionModel.class,
				SectionManagedObjectSourceFlowToFunctionEditPart.class);
		map.put(SectionManagedObjectDependencyToSectionManagedObjectModel.class,
				SectionManagedObjectDependencyToSectionManagedObjectEditPart.class);
		map.put(SectionManagedObjectDependencyToExternalManagedObjectModel.class,
				SectionManagedObjectDependencyToExternalManagedObjectEditPart.class);
		map.put(SectionManagedObjectToSectionManagedObjectSourceModel.class,
				SectionManagedObjectToSectionManagedObjectSourceEditPart.class);
		map.put(SubSectionObjectToExternalManagedObjectModel.class,
				SubSectionObjectToExternalManagedObjectEditPart.class);
		map.put(SubSectionObjectToSectionManagedObjectModel.class,
				SubSectionObjectToSectionManagedObjectEditPart.class);
		map.put(SubSectionOutputToSubSectionInputModel.class, SubSectionOutputToSubSectionInputEditPart.class);
		map.put(SubSectionOutputToExternalFlowModel.class, SubSectionOutputToExternalFlowEditPart.class);
		map.put(ManagedFunctionToFunctionModel.class, ManagedFunctionToFunctionEditPart.class);
		map.put(ManagedFunctionObjectToExternalManagedObjectModel.class,
				ManagedFunctionObjectToExternalManagedObjectEditPart.class);
		map.put(ManagedFunctionObjectToSectionManagedObjectModel.class,
				ManagedFunctionObjectToSectionManagedObjectEditPart.class);
		map.put(FunctionFlowToFunctionModel.class, FunctionFlowToFunctionEditPart.class);
		map.put(FunctionFlowToExternalFlowModel.class, FunctionFlowToExternalFlowEditPart.class);
		map.put(FunctionToNextFunctionModel.class, FunctionToNextFunctionEditPart.class);
		map.put(FunctionToNextExternalFlowModel.class, FunctionToNextExternalFlowEditPart.class);
		map.put(FunctionEscalationToFunctionModel.class, FunctionEscalationToFunctionEditPart.class);
		map.put(FunctionEscalationToExternalFlowModel.class, FunctionEscalationToExternalFlowEditPart.class);
	}

	@Override
	protected void initialisePaletteRoot() {
		// Add whether spawn thread state
		PaletteGroup linkGroup = new PaletteGroup("Function Flow");
		linkGroup.add(new ConnectionCreationToolEntry("Sequential", "sequential", new SpawnThreadStateTagFactory(false),
				null, null));
		linkGroup.add(
				new ConnectionCreationToolEntry("Spawn", "spawn", new SpawnThreadStateTagFactory(true), null, null));
		this.paletteRoot.add(linkGroup);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the section changes
		SectionChanges sectionChanges = this.getModelChanges();

		// Add operations
		list.add(new AddFunctionNamespaceOperation(sectionChanges));
		list.add(new AddSubSectionOperation(sectionChanges));
		list.add(new AddExternalFlowOperation(sectionChanges));
		list.add(new AddManagedObjectSourceOperation(sectionChanges));
		list.add(new AddExternalManagedObjectOperation(sectionChanges));

		// Add operations off added models
		list.add(new CreateFunctionFromManagedFunctionOperation(sectionChanges));
		list.add(new AddManagedObjectOperation(sectionChanges));

		// Change added model
		list.add(new RefactorFunctionNamespaceOperation(sectionChanges));
		list.add(new ToggleFunctionPublicOperation(sectionChanges));
		list.add(new ToggleManagedFunctionObjectParameterOperation(sectionChanges));
		list.add(new ToggleSubSectionInputPublicOperation(sectionChanges));

		// Delete actions
		list.add(new DeleteFunctionNamespaceOperation(sectionChanges));
		list.add(new DeleteSubSectionOperation(sectionChanges));
		list.add(new DeleteExternalFlowOperation(sectionChanges));
		list.add(new DeleteManagedObjectSourceOperation(sectionChanges));
		list.add(new DeleteExternalManagedObjectOperation(sectionChanges));
		list.add(new DeleteFunctionOperation(sectionChanges));
		list.add(new DeleteManagedObjectOperation(sectionChanges));
	}

	@Override
	protected void populateGraphicalEditPolicy(OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect managed function object to external managed object
		policy.addConnection(ManagedFunctionObjectModel.class, ExternalManagedObjectModel.class, (source, target,
				request) -> this.getModelChanges().linkManagedFunctionObjectToExternalManagedObject(source, target));

		// Connect managed function object to managed object
		policy.addConnection(ManagedFunctionObjectModel.class, SectionManagedObjectModel.class, (source, target,
				request) -> this.getModelChanges().linkManagedFunctionObjectToSectionManagedObject(source, target));

		// Connect function flow to function
		policy.addConnection(FunctionFlowModel.class, FunctionModel.class,
				(source, target, request) -> this.getModelChanges().linkFunctionFlowToFunction(source, target,
						this.isSpawnThreadState(request.getNewObject())));

		// Connect function flow to external flow
		policy.addConnection(FunctionFlowModel.class, ExternalFlowModel.class,
				(source, target, request) -> this.getModelChanges().linkFunctionFlowToExternalFlow(source, target,
						this.isSpawnThreadState(request.getNewObject())));

		// Connect function to next function
		policy.addConnection(FunctionModel.class, FunctionModel.class,
				(source, target, request) -> this.getModelChanges().linkFunctionToNextFunction(source, target));

		// Connect function to next external flow
		policy.addConnection(FunctionModel.class, ExternalFlowModel.class,
				(source, target, request) -> this.getModelChanges().linkFunctionToNextExternalFlow(source, target));

		// Connect function escalation to function
		policy.addConnection(FunctionEscalationModel.class, FunctionModel.class,
				(source, target, request) -> this.getModelChanges().linkFunctionEscalationToFunction(source, target));

		// Connect function escalation to external flow
		policy.addConnection(FunctionEscalationModel.class, ExternalFlowModel.class, (source, target, request) -> this
				.getModelChanges().linkFunctionEscalationToExternalFlow(source, target));

		// Connect sub section object to external managed object
		policy.addConnection(SubSectionObjectModel.class, ExternalManagedObjectModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSubSectionObjectToExternalManagedObject(source, target));

		// Connection sub section object to managed object
		policy.addConnection(SubSectionObjectModel.class, SectionManagedObjectModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSubSectionObjectToSectionManagedObject(source, target));

		// Connect sub section output to sub section input
		policy.addConnection(SubSectionOutputModel.class, SubSectionInputModel.class, (source, target,
				request) -> SectionEditor.this.getModelChanges().linkSubSectionOutputToSubSectionInput(source, target));

		// Connect sub section output to external flow
		policy.addConnection(SubSectionOutputModel.class, ExternalFlowModel.class, (source, target,
				request) -> SectionEditor.this.getModelChanges().linkSubSectionOutputToExternalFlow(source, target));

		// Connect managed object source flow to sub section input
		policy.addConnection(SectionManagedObjectSourceFlowModel.class, SubSectionInputModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSectionManagedObjectSourceFlowToSubSectionInput(source, target));

		// Connect managed object source flow to external flow
		policy.addConnection(SectionManagedObjectSourceFlowModel.class, ExternalFlowModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSectionManagedObjectSourceFlowToExternalFlow(source, target));

		// Connect managed object source flow to function
		policy.addConnection(SectionManagedObjectSourceFlowModel.class, FunctionModel.class, (source, target,
				request) -> this.getModelChanges().linkSectionManagedObjectSourceFlowToFunction(source, target));

		// Connect managed object dependency to managed object
		policy.addConnection(SectionManagedObjectDependencyModel.class, SectionManagedObjectModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSectionManagedObjectDependencyToSectionManagedObject(source, target));

		// Connect managed object dependency to external managed object
		policy.addConnection(SectionManagedObjectDependencyModel.class, ExternalManagedObjectModel.class,
				(source, target, request) -> SectionEditor.this.getModelChanges()
						.linkSectionManagedObjectDependencyToExternalManagedObject(source, target));
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting function namespace
		policy.addDelete(FunctionNamespaceModel.class,
				(target) -> this.getModelChanges().removeFunctionNamespace(target));

		// Allow deleting function
		policy.addDelete(FunctionModel.class, (target) -> this.getModelChanges().removeFunction(target));

		// Allow deleting function flow to function
		policy.addDelete(FunctionFlowToFunctionModel.class,
				(target) -> this.getModelChanges().removeFunctionFlowToFunction(target));

		// Allow deleting function flow to external flow
		policy.addDelete(FunctionFlowToExternalFlowModel.class,
				(target) -> this.getModelChanges().removeFunctionFlowToExternalFlow(target));

		// Allow deleting function to next function
		policy.addDelete(FunctionToNextFunctionModel.class,
				(target) -> this.getModelChanges().removeFunctionToNextFunction(target));

		// Allow deleting function to next external flow
		policy.addDelete(FunctionToNextExternalFlowModel.class,
				(target) -> this.getModelChanges().removeFunctionToNextExternalFlow(target));

		// Allow deleting function escalation to function
		policy.addDelete(FunctionEscalationToFunctionModel.class,
				(target) -> this.getModelChanges().removeFunctionEscalationToFunction(target));

		// Allow deleting function escalation to external flow
		policy.addDelete(FunctionEscalationToExternalFlowModel.class,
				(target) -> this.getModelChanges().removeFunctionEscalationToExternalFlow(target));

		// Allow deleting managed function object to external managed object
		policy.addDelete(ManagedFunctionObjectToExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeManagedFunctionObjectToExternalManagedObject(target));

		// Allow deleting managed function object to managed object
		policy.addDelete(ManagedFunctionObjectToSectionManagedObjectModel.class,
				(target) -> this.getModelChanges().removeManagedFunctionObjectToSectionManagedObject(target));

		// Allow deleting sub section
		policy.addDelete(SubSectionModel.class, (target) -> this.getModelChanges().removeSubSection(target));

		// Allow deleting external flow
		policy.addDelete(ExternalFlowModel.class, (target) -> this.getModelChanges().removeExternalFlow(target));

		// Allow deleting external managed object
		policy.addDelete(ExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeExternalManagedObject(target));

		// Allow deleting managed object source
		policy.addDelete(SectionManagedObjectSourceModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectSource(target));

		// Allow deleting managed object
		policy.addDelete(SectionManagedObjectModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObject(target));

		// Allow deleting sub section object to external managed object
		policy.addDelete(SubSectionObjectToExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeSubSectionObjectToExternalManagedObject(target));

		// Allow deleting sub section object to managed object
		policy.addDelete(SubSectionObjectToSectionManagedObjectModel.class,
				(target) -> this.getModelChanges().removeSubSectionObjectToSectionManagedObject(target));

		// Allow deleting sub section output to sub section input
		policy.addDelete(SubSectionOutputToSubSectionInputModel.class,
				(target) -> this.getModelChanges().removeSubSectionOutputToSubSectionInput(target));

		// Allow deleting sub section output to external flow
		policy.addDelete(SubSectionOutputToExternalFlowModel.class,
				(target) -> this.getModelChanges().removeSubSectionOutputToExternalFlow(target));

		// Allow deleting managed object source flow to sub section input
		policy.addDelete(SectionManagedObjectSourceFlowToSubSectionInputModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectSourceFlowToSubSectionInput(target));

		// Allow deleting managed object source flow to external flow
		policy.addDelete(SectionManagedObjectSourceFlowToExternalFlowModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectSourceFlowToExternalFlow(target));

		// Allow deleting managed object source flow to function
		policy.addDelete(SectionManagedObjectSourceFlowToFunctionModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectSourceFlowToFunction(target));

		// Allow deleting managed object dependency to managed object
		policy.addDelete(SectionManagedObjectDependencyToSectionManagedObjectModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectDependencyToSectionManagedObject(target));

		// Allow deleting managed object dependency to external managed object
		policy.addDelete(SectionManagedObjectDependencyToExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeSectionManagedObjectDependencyToExternalManagedObject(target));
	}

}