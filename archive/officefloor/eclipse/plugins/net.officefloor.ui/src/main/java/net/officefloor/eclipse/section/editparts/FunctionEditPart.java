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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.managedfunctionsource.FunctionDocumentationContext;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.skin.section.FunctionFigure;
import net.officefloor.eclipse.skin.section.FunctionFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionModel.FunctionEvent;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * {@link EditPart} for the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionEditPart extends AbstractOfficeFloorEditPart<FunctionModel, FunctionEvent, FunctionFigure>
		implements FunctionFigureContext {

	/**
	 * Obtains the {@link ManagedFunctionModel} for the {@link FunctionModel}.
	 * 
	 * @param function
	 *            {@link FunctionModel}.
	 * @return {@link ManagedFunctionModel} or <code>null</code> if not
	 *         attached.
	 */
	public static ManagedFunctionModel getManagedFunction(FunctionModel function) {
		// Ensure have task
		if (function == null) {
			return null;
		}

		// Obtain the managed function
		ManagedFunctionToFunctionModel conn = function.getManagedFunction();
		if (conn != null) {
			// Return the managed function
			return conn.getManagedFunction();
		}

		// If here then no work task attached
		return null;
	}

	/**
	 * Obtains the {@link SectionModel} for the {@link FunctionModel}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return {@link SectionModel} or <code>null</code> if
	 *         {@link FunctionModel} not contained in a {@link SectionModel}.
	 */
	public static SectionModel getSection(AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		// Obtain the desk (root model), ensuring is correct type
		Object rootModel = editPart.getEditor().getCastedModel();
		return (rootModel instanceof SectionModel ? (SectionModel) rootModel : null);
	}

	@Override
	protected FunctionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createFunctionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getFunctionFlows());
		childModels.addAll(this.getCastedModel().getFunctionEscalations());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getNextFunction());
		EclipseUtil.addToList(models, this.getCastedModel().getNextExternalFlow());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {

		// Add managed function
		EclipseUtil.addToList(models, this.getCastedModel().getManagedFunction());

		// Add task inputs, handled escalations, previous tasks
		models.addAll(this.getCastedModel().getFunctionFlowInputs());
		models.addAll(this.getCastedModel().getFunctionEscalationInputs());
		models.addAll(this.getCastedModel().getPreviousFunctions());

		// Add managed object source flows
		models.addAll(this.getCastedModel().getSectionManagedObjectSourceFlows());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<FunctionModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<SectionChanges, FunctionModel>() {
			@Override
			public String getInitialValue() {
				return FunctionEditPart.this.getCastedModel().getFunctionName();
			}

			@Override
			public IFigure getLocationFigure() {
				return FunctionEditPart.this.getOfficeFloorFigure().getFunctionNameFigure();
			}

			@Override
			public Change<FunctionModel> createChange(SectionChanges changes, FunctionModel target, String newValue) {
				return changes.renameFunction(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(OfficeFloorOpenEditPolicy<FunctionModel> policy) {
		policy.allowOpening(new OpenHandler<FunctionModel>() {
			@Override
			public void doOpen(OpenHandlerContext<FunctionModel> context) {

				// Obtain the section
				SectionModel section = FunctionEditPart.getSection(context.getEditPart());

				// Obtain the function namespace
				ManagedFunctionModel managedFunction = FunctionEditPart.getManagedFunction(context.getModel());
				FunctionNamespaceModel namespace = ManagedFunctionEditPart.getFunctionNamespace(managedFunction,
						section);

				// Ensure have the work
				if (namespace == null) {
					// Must have connected work
					context.getEditPart().messageError("Can not open function.\n"
							+ "\nPlease ensure the function is connected to a managed function source.");
					return; // can not open
				}

				// Open the namespace
				FunctionNamespaceEditPart.openManagedFunctionSource(namespace, context);
			}
		});
	}

	@Override
	protected Class<FunctionEvent> getPropertyChangeEventType() {
		return FunctionEvent.class;
	}

	@Override
	protected void handlePropertyChange(FunctionEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_FUNCTION_NAME:
			this.getOfficeFloorFigure().setFunctionName(this.getCastedModel().getFunctionName());
			break;

		case CHANGE_IS_PUBLIC:
			// Ensure display is public
			this.getOfficeFloorFigure().setIsPublic(this.getCastedModel().getIsPublic());
			break;

		case CHANGE_NEXT_FUNCTION:
		case CHANGE_NEXT_EXTERNAL_FLOW:
			this.refreshSourceConnections();
			break;

		case CHANGE_MANAGED_FUNCTION:
		case ADD_FUNCTION_FLOW_INPUT:
		case REMOVE_FUNCTION_FLOW_INPUT:
		case ADD_FUNCTION_ESCALATION_INPUT:
		case REMOVE_FUNCTION_ESCALATION_INPUT:
		case ADD_PREVIOUS_FUNCTION:
		case REMOVE_PREVIOUS_FUNCTION:
		case ADD_SECTION_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_SECTION_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshTargetConnections();
			break;

		case ADD_FUNCTION_FLOW:
		case REMOVE_FUNCTION_FLOW:
		case ADD_FUNCTION_ESCALATION:
		case REMOVE_FUNCTION_ESCALATION:
			this.refreshChildren();
			break;

		case CHANGE_RETURN_TYPE:
		case CHANGE_FUNCTION_NAMESPACE_NAME:
		case CHANGE_MANAGED_FUNCTION_NAME:
			// Non visual change
			break;
		}
	}

	/*
	 * ======================= FlowItemFigureContext ========================
	 */

	@Override
	public String getFunctionName() {
		return this.getCastedModel().getFunctionName();
	}

	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	@Override
	public void setIsPublic(final boolean isPublic) {

		// Store current state
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				FunctionEditPart.this.getCastedModel().setIsPublic(isPublic);
			}

			@Override
			protected void undoCommand() {
				FunctionEditPart.this.getCastedModel().setIsPublic(currentIsPublic);
			}
		});
	}

	@Override
	public String getParameterTypeName() {

		// Obtain the managed function for the function
		ManagedFunctionModel managedFunction = FunctionEditPart.getManagedFunction(this.getCastedModel());
		if (managedFunction != null) {
			// Have managed function, so find first parameter
			for (ManagedFunctionObjectModel object : managedFunction.getManagedFunctionObjects()) {
				if (object.getIsParameter()) {
					// Have parameter, so return its type
					return object.getObjectType();
				}
			}
		}

		// Did not find parameter
		return null;
	}

	@Override
	public String getReturnTypeName() {
		return this.getCastedModel().getReturnType();
	}

	@Override
	public String getFunctionDocumentation() {

		// Obtain the section
		SectionModel section = FunctionEditPart.getSection(this);

		// Return section documentation
		return FunctionDocumentationContextImpl.getFunctionDocumentation(section, this.getCastedModel(), this);
	}

	/**
	 * {@link FunctionDocumentationContext} implementation.
	 */
	private static class FunctionDocumentationContextImpl implements FunctionDocumentationContext {

		/**
		 * Obtains the documentation for a {@link FunctionModel}.
		 * 
		 * @param section
		 *            {@link SectionModel}.
		 * @param function
		 *            {@link FunctionModel}.
		 * @param editPart
		 *            {@link AbstractOfficeFloorEditPart}.
		 * @return Documentation for the {@link FunctionModel}.
		 */
		@SuppressWarnings("rawtypes")
		public static String getFunctionDocumentation(SectionModel section, FunctionModel function,
				AbstractOfficeFloorEditPart<?, ?, ?> editPart) {

			// Obtain the namespace and managed function for the function
			ManagedFunctionModel managedFunction = FunctionEditPart.getManagedFunction(function);
			FunctionNamespaceModel namespace = ManagedFunctionEditPart.getFunctionNamespace(managedFunction, section);
			if (namespace == null) {
				// Can not obtain namespace, so provide available documentation
				return "Function " + function.getFunctionName()
						+ " is not associated with any Namespace.\n\nPlease ensure it is associated to Namespace.";

			}

			// Obtain the managed function source details
			String managedFunctionSourceClassName = namespace.getManagedFunctionSourceClassName();
			if (EclipseUtil.isBlank(managedFunctionSourceClassName)) {
				// Must have managed function source class name
				return "Function " + function.getFunctionName() + " runs function "
						+ managedFunction.getManagedFunctionName() + " in namespace "
						+ namespace.getFunctionNamespaceName() + "\n\nThe namespace however does not specify a "
						+ ManagedFunctionSource.class.getSimpleName();
			}

			// Determine if a managed function source extension for namespace
			Map<String, ManagedFunctionSourceExtension> extensions = ExtensionUtil
					.createManagedFunctionSourceExtensionMap();
			ManagedFunctionSourceExtension<?> extension = extensions.get(managedFunctionSourceClassName);

			// Obtain the function documentation
			String functionDocumentation = null;
			if (extension != null) {
				try {

					// Obtain the managed function name
					String managedFunctionName = managedFunction.getManagedFunctionName();

					// Obtain the property list
					PropertyList properties = OfficeFloorCompiler.newPropertyList();
					for (PropertyModel property : namespace.getProperties()) {
						properties.addProperty(property.getName()).setValue(property.getValue());
					}

					// Obtain the class loader
					ClassLoader classLoader = ProjectClassLoader.create(editPart.getEditor());

					// Create the context
					FunctionDocumentationContext context = new FunctionDocumentationContextImpl(managedFunctionName,
							properties, classLoader);

					// Obtain documentation from extension
					functionDocumentation = extension.getFunctionDocumentation(context);

				} catch (Throwable ex) {
					// No documentation for extension
					functionDocumentation = null;
				}
			}
			if (EclipseUtil.isBlank(functionDocumentation)) {
				// No extension, no documentation or failure from extension
				functionDocumentation = "Function " + managedFunction.getManagedFunctionName() + " of source "
						+ managedFunctionSourceClassName;

			}

			// Return the task documentation
			return functionDocumentation;
		}

		/**
		 * {@link FunctionModel} name.
		 */
		private final String functionName;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList propertyList;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Initiate.
		 * 
		 * @param functionName
		 *            {@link FunctionModel} name.
		 * @param propertyList
		 *            {@link PropertyList}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public FunctionDocumentationContextImpl(String functionName, PropertyList propertyList,
				ClassLoader classLoader) {
			this.functionName = functionName;
			this.propertyList = propertyList;
			this.classLoader = classLoader;
		}

		/*
		 * ==================== TaskDocumentationContext ==================
		 */

		@Override
		public String getManagedFunctionName() {
			return this.functionName;
		}

		@Override
		public PropertyList getPropertyList() {
			return this.propertyList;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}