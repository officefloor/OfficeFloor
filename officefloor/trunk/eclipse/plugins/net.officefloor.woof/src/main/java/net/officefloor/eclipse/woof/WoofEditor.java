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
package net.officefloor.eclipse.woof;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.ChildEditPolicyFactory;
import net.officefloor.eclipse.common.editpolicies.layout.ConstraintChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.DeleteChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.woof.editparts.WoofAccessEditPart;
import net.officefloor.eclipse.woof.editparts.WoofAccessInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofAccessOutputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofAccessOutputToWoofResourceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofAccessOutputToWoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofAccessOutputToWoofTemplateEditPart;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.eclipse.woof.editparts.WoofExceptionEditPart;
import net.officefloor.eclipse.woof.editparts.WoofExceptionToWoofResourceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofExceptionToWoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofExceptionToWoofTemplateEditPart;
import net.officefloor.eclipse.woof.editparts.WoofGovernanceAreaEditPart;
import net.officefloor.eclipse.woof.editparts.WoofGovernanceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofGovernanceToWoofGovernanceAreaEditPart;
import net.officefloor.eclipse.woof.editparts.WoofResourceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionOutputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionOutputToWoofAccessInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionOutputToWoofResourceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionOutputToWoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofSectionOutputToWoofTemplateEditPart;
import net.officefloor.eclipse.woof.editparts.WoofStartEditPart;
import net.officefloor.eclipse.woof.editparts.WoofStartToWoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputToWoofAccessInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputToWoofResourceEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputToWoofSectionInputEditPart;
import net.officefloor.eclipse.woof.editparts.WoofTemplateOutputToWoofTemplateEditPart;
import net.officefloor.eclipse.woof.operations.AddExceptionOperation;
import net.officefloor.eclipse.woof.operations.AddGovernanceAreaOperation;
import net.officefloor.eclipse.woof.operations.AddGovernanceOperation;
import net.officefloor.eclipse.woof.operations.AddResourceOperation;
import net.officefloor.eclipse.woof.operations.AddSectionOperation;
import net.officefloor.eclipse.woof.operations.AddStartOperation;
import net.officefloor.eclipse.woof.operations.AddTemplateOperation;
import net.officefloor.eclipse.woof.operations.DeleteAccessOperation;
import net.officefloor.eclipse.woof.operations.DeleteExceptionOperation;
import net.officefloor.eclipse.woof.operations.DeleteGovernanceAreaOperation;
import net.officefloor.eclipse.woof.operations.DeleteGovernanceOperation;
import net.officefloor.eclipse.woof.operations.DeleteResourceOperation;
import net.officefloor.eclipse.woof.operations.DeleteSectionOperation;
import net.officefloor.eclipse.woof.operations.DeleteStartOperation;
import net.officefloor.eclipse.woof.operations.DeleteTemplateOperation;
import net.officefloor.eclipse.woof.operations.RefactorAccessOperation;
import net.officefloor.eclipse.woof.operations.RefactorExceptionOperation;
import net.officefloor.eclipse.woof.operations.RefactorGovernanceOperation;
import net.officefloor.eclipse.woof.operations.RefactorResourceOperation;
import net.officefloor.eclipse.woof.operations.RefactorSectionOperation;
import net.officefloor.eclipse.woof.operations.RefactorTemplateOperation;
import net.officefloor.eclipse.woof.operations.SetAccessOperation;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofAccessInputModel;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofAccessOutputModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofChangesImpl;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofGovernanceModel;
import net.officefloor.model.woof.WoofGovernanceToWoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofResourceModel;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofSectionOutputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofAccessInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofStartModel;
import net.officefloor.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateChangeContext;
import net.officefloor.model.woof.WoofTemplateChangeContextImpl;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofAccessInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.plugin.woof.WoofContextConfigurable;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
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

	/**
	 * Obtains the {@link WoofTemplateChangeContext}.
	 * 
	 * @return {@link WoofTemplateChangeContext}.
	 */
	public WoofTemplateChangeContext getWoofTemplateChangeContext() {

		// Create the configuration context
		ProjectConfigurationContext configurationContext = new ProjectConfigurationContext(
				WoofEditor.this.getEditorInput());

		// Obtain the resource context
		final List<ResourceSource> resourceSources = new LinkedList<ResourceSource>();
		WoofContextConfigurable configurable = new WoofContextConfigurable() {
			@Override
			public void addProperty(String name, String value) {
				// No properties required
			}

			@Override
			public void setWebAppDirectory(File webAppDir) {
				// Not required
			}

			@Override
			public void addResources(ResourceSource resourceSource) {
				resourceSources.add(resourceSource);
			}
		};
		File projectDir = configurationContext.getProject().getLocation()
				.toFile();
		WoofOfficeFloorSource.loadWebResourcesFromMavenProject(configurable,
				projectDir);

		// Create the change context
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		WoofTemplateChangeContext context = new WoofTemplateChangeContextImpl(
				true, classLoader, configurationContext,
				resourceSources.toArray(new ResourceSource[resourceSources
						.size()]));

		// Return the change context
		return context;
	}

	/*
	 * ======================== Editor ================================
	 */

	@Override
	protected WoofChanges createModelChanges(WoofModel model) {
		// Create and return the WoOF changes
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
		map.put(WoofSectionModel.class, WoofSectionEditPart.class);
		map.put(WoofSectionInputModel.class, WoofSectionInputEditPart.class);
		map.put(WoofSectionOutputModel.class, WoofSectionOutputEditPart.class);
		map.put(WoofAccessModel.class, WoofAccessEditPart.class);
		map.put(WoofAccessInputModel.class, WoofAccessInputEditPart.class);
		map.put(WoofAccessOutputModel.class, WoofAccessOutputEditPart.class);
		map.put(WoofGovernanceModel.class, WoofGovernanceEditPart.class);
		map.put(WoofGovernanceAreaModel.class, WoofGovernanceAreaEditPart.class);
		map.put(WoofResourceModel.class, WoofResourceEditPart.class);
		map.put(WoofExceptionModel.class, WoofExceptionEditPart.class);
		map.put(WoofStartModel.class, WoofStartEditPart.class);

		// Connections
		map.put(WoofTemplateOutputToWoofTemplateModel.class,
				WoofTemplateOutputToWoofTemplateEditPart.class);
		map.put(WoofTemplateOutputToWoofSectionInputModel.class,
				WoofTemplateOutputToWoofSectionInputEditPart.class);
		map.put(WoofTemplateOutputToWoofAccessInputModel.class,
				WoofTemplateOutputToWoofAccessInputEditPart.class);
		map.put(WoofTemplateOutputToWoofResourceModel.class,
				WoofTemplateOutputToWoofResourceEditPart.class);
		map.put(WoofSectionOutputToWoofTemplateModel.class,
				WoofSectionOutputToWoofTemplateEditPart.class);
		map.put(WoofSectionOutputToWoofSectionInputModel.class,
				WoofSectionOutputToWoofSectionInputEditPart.class);
		map.put(WoofSectionOutputToWoofAccessInputModel.class,
				WoofSectionOutputToWoofAccessInputEditPart.class);
		map.put(WoofSectionOutputToWoofResourceModel.class,
				WoofSectionOutputToWoofResourceEditPart.class);
		map.put(WoofAccessOutputToWoofTemplateModel.class,
				WoofAccessOutputToWoofTemplateEditPart.class);
		map.put(WoofAccessOutputToWoofSectionInputModel.class,
				WoofAccessOutputToWoofSectionInputEditPart.class);
		map.put(WoofAccessOutputToWoofResourceModel.class,
				WoofAccessOutputToWoofResourceEditPart.class);
		map.put(WoofGovernanceToWoofGovernanceAreaModel.class,
				WoofGovernanceToWoofGovernanceAreaEditPart.class);
		map.put(WoofExceptionToWoofTemplateModel.class,
				WoofExceptionToWoofTemplateEditPart.class);
		map.put(WoofExceptionToWoofSectionInputModel.class,
				WoofExceptionToWoofSectionInputEditPart.class);
		map.put(WoofExceptionToWoofResourceModel.class,
				WoofExceptionToWoofResourceEditPart.class);
		map.put(WoofStartToWoofSectionInputModel.class,
				WoofStartToWoofSectionInputEditPart.class);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the WoOF changes
		WoofChanges woofChanges = this.getModelChanges();

		// Template actions
		list.add(new AddTemplateOperation(woofChanges, this));
		list.add(new RefactorTemplateOperation(woofChanges, this));
		list.add(new DeleteTemplateOperation(woofChanges, this));

		// Section actions
		list.add(new AddSectionOperation(woofChanges));
		list.add(new RefactorSectionOperation(woofChanges));
		list.add(new DeleteSectionOperation(woofChanges));

		// Access actions
		list.add(new SetAccessOperation(woofChanges));
		list.add(new RefactorAccessOperation(woofChanges));
		list.add(new DeleteAccessOperation(woofChanges));

		// Governance actions
		list.add(new AddGovernanceOperation(woofChanges));
		list.add(new RefactorGovernanceOperation(woofChanges));
		list.add(new DeleteGovernanceOperation(woofChanges));
		list.add(new AddGovernanceAreaOperation(woofChanges));
		list.add(new DeleteGovernanceAreaOperation(woofChanges));

		// Resource actions
		list.add(new AddResourceOperation(woofChanges));
		list.add(new RefactorResourceOperation(woofChanges));
		list.add(new DeleteResourceOperation(woofChanges));

		// Exception actions
		list.add(new AddExceptionOperation(woofChanges));
		list.add(new RefactorExceptionOperation(woofChanges));
		list.add(new DeleteExceptionOperation(woofChanges));

		// Start actions
		list.add(new AddStartOperation(woofChanges));
		list.add(new DeleteStartOperation(woofChanges));
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Provide default child edit policy
		policy.setDefaultChild(new ChildEditPolicyFactory<Object>() {
			@Override
			public EditPolicy createEditPolicy(Object target) {
				return new WoofNonResizableEditPolicy();
			}
		});

		// Allow resizing governance area
		policy.addConstraint(WoofGovernanceAreaModel.class,
				new ConstraintChangeFactory<WoofGovernanceAreaModel>() {
					@Override
					public Change<WoofGovernanceAreaModel> createChange(
							WoofGovernanceAreaModel target, Rectangle constraint) {
						return new ResizeWoofGovernanceAreaChange(target,
								constraint);
					}
				});
		policy.addChild(WoofGovernanceAreaModel.class,
				new ChildEditPolicyFactory<WoofGovernanceAreaModel>() {
					@Override
					public EditPolicy createEditPolicy(
							WoofGovernanceAreaModel target) {
						return new WoofResizableEditPolicy();
					}
				});

		// Allow deleting template
		policy.addDelete(WoofTemplateModel.class,
				new DeleteChangeFactory<WoofTemplateModel>() {
					@Override
					public Change<WoofTemplateModel> createChange(
							WoofTemplateModel target) {
						// Return the change to remove the template
						return WoofEditor.this
								.getModelChanges()
								.removeTemplate(
										target,
										WoofEditor.this
												.getWoofTemplateChangeContext());
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

		// Allow deleting access
		policy.addDelete(WoofAccessModel.class,
				new DeleteChangeFactory<WoofAccessModel>() {
					@Override
					public Change<WoofAccessModel> createChange(
							WoofAccessModel target) {
						return WoofEditor.this.getModelChanges().removeAccess(
								target);
					}
				});

		// Allow deleting governance
		policy.addDelete(WoofGovernanceModel.class,
				new DeleteChangeFactory<WoofGovernanceModel>() {
					@Override
					public Change<WoofGovernanceModel> createChange(
							WoofGovernanceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeGovernance(target);
					}
				});

		// Allow deleting governance area
		policy.addDelete(WoofGovernanceAreaModel.class,
				new DeleteChangeFactory<WoofGovernanceAreaModel>() {
					@Override
					public Change<WoofGovernanceAreaModel> createChange(
							WoofGovernanceAreaModel target) {
						return WoofEditor.this.getModelChanges()
								.removeGovernanceArea(target);
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

		// Allow deleting start
		policy.addDelete(WoofStartModel.class,
				new DeleteChangeFactory<WoofStartModel>() {
					@Override
					public Change<WoofStartModel> createChange(
							WoofStartModel target) {
						return WoofEditor.this.getModelChanges().removeStart(
								target);
					}
				});

		// Allow deleting template output to template
		policy.addDelete(
				WoofTemplateOutputToWoofTemplateModel.class,
				new DeleteChangeFactory<WoofTemplateOutputToWoofTemplateModel>() {
					@Override
					public Change<WoofTemplateOutputToWoofTemplateModel> createChange(
							WoofTemplateOutputToWoofTemplateModel target) {
						return WoofEditor.this.getModelChanges()
								.removeTemplateOuputToTemplate(target);
					}
				});

		// Allow deleting template output to section input
		policy.addDelete(
				WoofTemplateOutputToWoofSectionInputModel.class,
				new DeleteChangeFactory<WoofTemplateOutputToWoofSectionInputModel>() {
					@Override
					public Change<WoofTemplateOutputToWoofSectionInputModel> createChange(
							WoofTemplateOutputToWoofSectionInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeTemplateOuputToSectionInput(target);
					}
				});

		// Allow deleting template output to access input
		policy.addDelete(
				WoofTemplateOutputToWoofAccessInputModel.class,
				new DeleteChangeFactory<WoofTemplateOutputToWoofAccessInputModel>() {
					@Override
					public Change<WoofTemplateOutputToWoofAccessInputModel> createChange(
							WoofTemplateOutputToWoofAccessInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeTemplateOuputToAccessInput(target);
					}
				});

		// Allow deleting template output to resource
		policy.addDelete(
				WoofTemplateOutputToWoofResourceModel.class,
				new DeleteChangeFactory<WoofTemplateOutputToWoofResourceModel>() {
					@Override
					public Change<WoofTemplateOutputToWoofResourceModel> createChange(
							WoofTemplateOutputToWoofResourceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeTemplateOuputToResource(target);
					}
				});

		// Allow deleting section output to template
		policy.addDelete(
				WoofSectionOutputToWoofTemplateModel.class,
				new DeleteChangeFactory<WoofSectionOutputToWoofTemplateModel>() {
					@Override
					public Change<WoofSectionOutputToWoofTemplateModel> createChange(
							WoofSectionOutputToWoofTemplateModel target) {
						return WoofEditor.this.getModelChanges()
								.removeSectionOuputToTemplate(target);
					}
				});

		// Allow deleting section output to section input
		policy.addDelete(
				WoofSectionOutputToWoofSectionInputModel.class,
				new DeleteChangeFactory<WoofSectionOutputToWoofSectionInputModel>() {
					@Override
					public Change<WoofSectionOutputToWoofSectionInputModel> createChange(
							WoofSectionOutputToWoofSectionInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeSectionOuputToSectionInput(target);
					}
				});

		// Allow deleting section output to access input
		policy.addDelete(
				WoofSectionOutputToWoofAccessInputModel.class,
				new DeleteChangeFactory<WoofSectionOutputToWoofAccessInputModel>() {
					@Override
					public Change<WoofSectionOutputToWoofAccessInputModel> createChange(
							WoofSectionOutputToWoofAccessInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeSectionOuputToAccessInput(target);
					}
				});

		// Allow deleting section output to resource
		policy.addDelete(
				WoofSectionOutputToWoofResourceModel.class,
				new DeleteChangeFactory<WoofSectionOutputToWoofResourceModel>() {
					@Override
					public Change<WoofSectionOutputToWoofResourceModel> createChange(
							WoofSectionOutputToWoofResourceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeSectionOuputToResource(target);
					}
				});

		// Allow deleting access output to template
		policy.addDelete(WoofAccessOutputToWoofTemplateModel.class,
				new DeleteChangeFactory<WoofAccessOutputToWoofTemplateModel>() {
					@Override
					public Change<WoofAccessOutputToWoofTemplateModel> createChange(
							WoofAccessOutputToWoofTemplateModel target) {
						return WoofEditor.this.getModelChanges()
								.removeAccessOuputToTemplate(target);
					}
				});

		// Allow deleting access output to section input
		policy.addDelete(
				WoofAccessOutputToWoofSectionInputModel.class,
				new DeleteChangeFactory<WoofAccessOutputToWoofSectionInputModel>() {
					@Override
					public Change<WoofAccessOutputToWoofSectionInputModel> createChange(
							WoofAccessOutputToWoofSectionInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeAccessOuputToSectionInput(target);
					}
				});

		// Allow deleting access output to resource
		policy.addDelete(WoofAccessOutputToWoofResourceModel.class,
				new DeleteChangeFactory<WoofAccessOutputToWoofResourceModel>() {
					@Override
					public Change<WoofAccessOutputToWoofResourceModel> createChange(
							WoofAccessOutputToWoofResourceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeAccessOuputToResource(target);
					}
				});

		// Allow deleting exception to template
		policy.addDelete(WoofExceptionToWoofTemplateModel.class,
				new DeleteChangeFactory<WoofExceptionToWoofTemplateModel>() {
					@Override
					public Change<WoofExceptionToWoofTemplateModel> createChange(
							WoofExceptionToWoofTemplateModel target) {
						return WoofEditor.this.getModelChanges()
								.removeExceptionToTemplate(target);
					}
				});

		// Allow deleting exception to section input
		policy.addDelete(
				WoofExceptionToWoofSectionInputModel.class,
				new DeleteChangeFactory<WoofExceptionToWoofSectionInputModel>() {
					@Override
					public Change<WoofExceptionToWoofSectionInputModel> createChange(
							WoofExceptionToWoofSectionInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeExceptionToSectionInput(target);
					}
				});

		// Allow deleting exception to resource
		policy.addDelete(WoofExceptionToWoofResourceModel.class,
				new DeleteChangeFactory<WoofExceptionToWoofResourceModel>() {
					@Override
					public Change<WoofExceptionToWoofResourceModel> createChange(
							WoofExceptionToWoofResourceModel target) {
						return WoofEditor.this.getModelChanges()
								.removeExceptionToResource(target);
					}
				});

		// Allow deleting start to section input
		policy.addDelete(WoofStartToWoofSectionInputModel.class,
				new DeleteChangeFactory<WoofStartToWoofSectionInputModel>() {
					@Override
					public Change<WoofStartToWoofSectionInputModel> createChange(
							WoofStartToWoofSectionInputModel target) {
						return WoofEditor.this.getModelChanges()
								.removeStartToSectionInput(target);
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
						return WoofEditor.this.getModelChanges()
								.linkTemplateOutputToTemplate(source, target);
					}
				});

		// Connect template output to section input
		policy.addConnection(
				WoofTemplateOutputModel.class,
				WoofSectionInputModel.class,
				new ConnectionChangeFactory<WoofTemplateOutputModel, WoofSectionInputModel>() {
					@Override
					public Change<?> createChange(
							WoofTemplateOutputModel source,
							WoofSectionInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkTemplateOutputToSectionInput(source,
										target);
					}
				});

		// Connect template output to access input
		policy.addConnection(
				WoofTemplateOutputModel.class,
				WoofAccessInputModel.class,
				new ConnectionChangeFactory<WoofTemplateOutputModel, WoofAccessInputModel>() {
					@Override
					public Change<?> createChange(
							WoofTemplateOutputModel source,
							WoofAccessInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this
								.getModelChanges()
								.linkTemplateOutputToAccessInput(source, target);
					}
				});

		// Connect template output to resource
		policy.addConnection(
				WoofTemplateOutputModel.class,
				WoofResourceModel.class,
				new ConnectionChangeFactory<WoofTemplateOutputModel, WoofResourceModel>() {
					@Override
					public Change<?> createChange(
							WoofTemplateOutputModel source,
							WoofResourceModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkTemplateOutputToResource(source, target);
					}
				});

		// Connect section output to template
		policy.addConnection(
				WoofSectionOutputModel.class,
				WoofTemplateModel.class,
				new ConnectionChangeFactory<WoofSectionOutputModel, WoofTemplateModel>() {
					@Override
					public Change<?> createChange(
							WoofSectionOutputModel source,
							WoofTemplateModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkSectionOutputToTemplate(source, target);
					}
				});

		// Connect section output to section input
		policy.addConnection(
				WoofSectionOutputModel.class,
				WoofSectionInputModel.class,
				new ConnectionChangeFactory<WoofSectionOutputModel, WoofSectionInputModel>() {
					@Override
					public Change<?> createChange(
							WoofSectionOutputModel source,
							WoofSectionInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this
								.getModelChanges()
								.linkSectionOutputToSectionInput(source, target);
					}
				});

		// Connect section output to access input
		policy.addConnection(
				WoofSectionOutputModel.class,
				WoofAccessInputModel.class,
				new ConnectionChangeFactory<WoofSectionOutputModel, WoofAccessInputModel>() {
					@Override
					public Change<?> createChange(
							WoofSectionOutputModel source,
							WoofAccessInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkSectionOutputToAccessInput(source, target);
					}
				});

		// Connect section output to resource
		policy.addConnection(
				WoofSectionOutputModel.class,
				WoofResourceModel.class,
				new ConnectionChangeFactory<WoofSectionOutputModel, WoofResourceModel>() {
					@Override
					public Change<?> createChange(
							WoofSectionOutputModel source,
							WoofResourceModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkSectionOutputToResource(source, target);
					}
				});

		// Connect access output to template
		policy.addConnection(
				WoofAccessOutputModel.class,
				WoofTemplateModel.class,
				new ConnectionChangeFactory<WoofAccessOutputModel, WoofTemplateModel>() {
					@Override
					public Change<?> createChange(WoofAccessOutputModel source,
							WoofTemplateModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkAccessOutputToTemplate(source, target);
					}
				});

		// Connect access output to section input
		policy.addConnection(
				WoofAccessOutputModel.class,
				WoofSectionInputModel.class,
				new ConnectionChangeFactory<WoofAccessOutputModel, WoofSectionInputModel>() {
					@Override
					public Change<?> createChange(WoofAccessOutputModel source,
							WoofSectionInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkAccessOutputToSectionInput(source, target);
					}
				});

		// Connect access output to resource
		policy.addConnection(
				WoofAccessOutputModel.class,
				WoofResourceModel.class,
				new ConnectionChangeFactory<WoofAccessOutputModel, WoofResourceModel>() {
					@Override
					public Change<?> createChange(WoofAccessOutputModel source,
							WoofResourceModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkAccessOutputToResource(source, target);
					}
				});

		// Connect exception to template
		policy.addConnection(
				WoofExceptionModel.class,
				WoofTemplateModel.class,
				new ConnectionChangeFactory<WoofExceptionModel, WoofTemplateModel>() {
					@Override
					public Change<?> createChange(WoofExceptionModel source,
							WoofTemplateModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkExceptionToTemplate(source, target);
					}
				});

		// Connect exception to section input
		policy.addConnection(
				WoofExceptionModel.class,
				WoofSectionInputModel.class,
				new ConnectionChangeFactory<WoofExceptionModel, WoofSectionInputModel>() {
					@Override
					public Change<?> createChange(WoofExceptionModel source,
							WoofSectionInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkExceptionToSectionInput(source, target);
					}
				});

		// Connect exception to resource
		policy.addConnection(
				WoofExceptionModel.class,
				WoofResourceModel.class,
				new ConnectionChangeFactory<WoofExceptionModel, WoofResourceModel>() {
					@Override
					public Change<?> createChange(WoofExceptionModel source,
							WoofResourceModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkExceptionToResource(source, target);
					}
				});

		// Connect start to section input
		policy.addConnection(
				WoofStartModel.class,
				WoofSectionInputModel.class,
				new ConnectionChangeFactory<WoofStartModel, WoofSectionInputModel>() {
					@Override
					public Change<?> createChange(WoofStartModel source,
							WoofSectionInputModel target,
							CreateConnectionRequest request) {
						return WoofEditor.this.getModelChanges()
								.linkStartToSectionInput(source, target);
					}
				});
	}

}