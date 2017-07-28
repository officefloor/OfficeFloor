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
package net.officefloor.eclipse.common.editor;

import java.awt.Dialog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.action.OperationAction;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.CommonGraphicalViewerKeyHandler;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.Model;

/**
 * Provides an abstract {@link GraphicalEditor} for the Office Floor items to
 * edit.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorEditor<M extends Model, C> extends GraphicalEditorWithFlyoutPalette
		implements EditPartFactory {

	/**
	 * Root {@link Model} being edited.
	 */
	private M rootModel;

	/**
	 * Root {@link EditPart}.
	 */
	private RootEditPart rootEditPart;

	/**
	 * Provides change functionality for the {@link Model}.
	 */
	private C modelChanges;

	/**
	 * Map of {@link Model} type to {@link EditPart} type.
	 */
	private Map<Class<?>, Class<? extends EditPart>> modelTypeToEditPartTypeMap = new HashMap<Class<?>, Class<? extends EditPart>>();

	/**
	 * Listing of the {@link Operation} instances for the context {@link Menu}.
	 */
	private Operation[] operations;

	/**
	 * {@link PaletteRoot}.
	 */
	protected PaletteRoot paletteRoot = null;

	/**
	 * Initiate the edit domain and the command stack.
	 */
	public AbstractOfficeFloorEditor() {
		// Specify the Edit Domain
		DefaultEditDomain editDomain = new DefaultEditDomain(this);
		editDomain.setDefaultTool(new SelectionTool());
		editDomain.loadDefaultTool();
		this.setEditDomain(editDomain);

		// Set up the command stack
		CommandStack commandStack = new CommandStack();
		editDomain.setCommandStack(commandStack);
		commandStack.addCommandStackEventListener(new CommandStackEventListener() {
			@Override
			public void stackChanged(CommandStackEvent event) {
				// Update property dependent actions
				AbstractOfficeFloorEditor.this.updateActions(AbstractOfficeFloorEditor.this.getPropertyActions());

				// Notify change in dirty state
				AbstractOfficeFloorEditor.this.firePropertyChange(PROP_DIRTY);
			}
		});
	}

	/**
	 * Specifies the Model.
	 * 
	 * @param model
	 *            Model.
	 */
	protected void setCastedModel(M model) {
		this.rootModel = model;
	}

	/**
	 * Obtains the {@link Model}.
	 * 
	 * @return {@link Model}.
	 */
	public M getCastedModel() {
		return this.rootModel;
	}

	/**
	 * Obtains the {@link RootEditPart}.
	 * 
	 * @return {@link RootEditPart}.
	 */
	public RootEditPart getRootEditPart() {
		return this.rootEditPart;
	}

	/**
	 * Obtains the {@link Model} change functionality.
	 * 
	 * @return {@link Model} change functionality.
	 */
	public C getModelChanges() {
		return this.modelChanges;
	}

	/**
	 * Displays the {@link Throwable} error details as an error
	 * {@link MessageDialog}.
	 * 
	 * @param error
	 *            Error.
	 */
	public void messageError(Throwable error) {

		// Obtain the location of the error
		final int DEPTH = 10;
		StringBuilder location = new StringBuilder();
		StackTraceElement[] stackTrace = error.getStackTrace();
		for (int i = 0; i < DEPTH; i++) {
			if (i < stackTrace.length) {
				location.append("\n " + stackTrace[i].toString());
			}
		}
		if (DEPTH < stackTrace.length) {
			location.append("\n ...");
		}

		this.messageError(new Status(IStatus.ERROR, OfficeFloorPlugin.PLUGIN_ID,
				error.getClass().getSimpleName() + ": " + error.getMessage() + "\n" + location.toString(), error));
	}

	/**
	 * Displays the message as an error {@link MessageDialog}.
	 * 
	 * @param message
	 *            Error message.
	 */
	public void messageError(String message) {
		this.messageError(new Status(IStatus.ERROR, OfficeFloorPlugin.PLUGIN_ID, message));
	}

	/**
	 * Displays the message and its cause as an error {@link MessageDialog}.
	 * 
	 * @param message
	 *            Error message.
	 * @param cause
	 *            Cause of error.
	 */
	public void messageError(String message, Throwable cause) {
		this.messageError(message + "\n\n" + cause.getClass().getSimpleName() + ": " + cause.getMessage());
	}

	/**
	 * Displays the message as a warning {@link MessageDialog}.
	 * 
	 * @param message
	 *            Warning message
	 */
	public void messageWarning(String message) {
		this.messageStatus(new Status(IStatus.WARNING, OfficeFloorPlugin.PLUGIN_ID, message), "Warning");
	}

	/**
	 * Displays the {@link IStatus} error.
	 * 
	 * @param status
	 *            {@link IStatus} error.
	 */
	public void messageError(IStatus status) {
		this.messageStatus(status, "Error");
	}

	/**
	 * Displays a {@link Dialog} for the {@link IStatus}.
	 * 
	 * @param status
	 *            {@link IStatus}.
	 * @param title
	 *            Title for {@link Dialog}.
	 */
	public void messageStatus(IStatus status, String title) {
		ErrorDialog.openError(this.getEditorSite().getShell(), title, null, status);
	}

	/**
	 * Creates the {@link LayoutEditPolicy} to be installed.
	 * 
	 * @return {@link LayoutEditPolicy} to be installed.
	 */
	public LayoutEditPolicy createLayoutEditPolicy() {
		OfficeFloorLayoutEditPolicy policy = new OfficeFloorLayoutEditPolicy();
		this.populateLayoutEditPolicy(policy);
		return policy;
	}

	/**
	 * Populates the {@link OfficeFloorLayoutEditPolicy}.
	 * 
	 * @param policy
	 *            {@link OfficeFloorLayoutEditPolicy}.
	 */
	protected abstract void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy);

	/**
	 * Creates the {@link GraphicalEditPolicy} to be installed.
	 * 
	 * @return {@link GraphicalEditPolicy} to be installed.
	 */
	public GraphicalNodeEditPolicy createGraphicalEditPolicy() {
		OfficeFloorGraphicalNodeEditPolicy policy = new OfficeFloorGraphicalNodeEditPolicy();
		this.populateGraphicalEditPolicy(policy);
		return policy;
	}

	/**
	 * Populates the {@link OfficeFloorGraphicalNodeEditPolicy}.
	 * 
	 * @param policy
	 *            {@link OfficeFloorGraphicalNodeEditPolicy}.
	 */
	protected abstract void populateGraphicalEditPolicy(OfficeFloorGraphicalNodeEditPolicy policy);

	/*
	 * =================== GraphicalEditor =========================
	 */

	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();

		// Make the root edit part available for return
		this.rootEditPart = new ScalableFreeformRootEditPart();

		// Initialise the graphical viewer
		GraphicalViewer viewer = this.getGraphicalViewer();
		viewer.setRootEditPart(this.rootEditPart);
		viewer.setKeyHandler(new CommonGraphicalViewerKeyHandler(viewer));

		// Load the edit part factory and initialise contents
		this.loadEditPartTypes();
		viewer.setEditPartFactory(this);
		viewer.setContents(this.getCastedModel());

		// Initialise the context menu
		this.initialiseContextMenu();
	}

	/**
	 * {@link MouseListener} that listens to last location the mouse down event
	 * was fired so that context menu may obtain the location.
	 */
	private class MouseLocation extends MouseAdapter {

		/**
		 * Last X location.
		 */
		private int lastX = -1;

		/**
		 * Last Y location
		 */
		private int lastY = -1;

		@Override
		public void mouseDown(MouseEvent e) {
			this.lastX = e.x;
			this.lastY = e.y;
		}

		/**
		 * Obtains the location of the mouse for the down event.
		 * 
		 * @return Location of the mouse for the down event.
		 */
		public Point getLocation() {
			return new Point(this.lastX, this.lastY);
		}
	}

	/**
	 * Initialises the context menu.
	 */
	protected void initialiseContextMenu() {

		// Obtain the listing of operations
		List<Operation> operationList = new LinkedList<Operation>();
		this.populateOperations(operationList);
		this.operations = operationList.toArray(new Operation[0]);

		// Ensure have operations
		if (this.operations.length == 0) {
			// No operations therefore do not provide context menu
			return;
		}

		// Add mouse listener for the location to give operations
		final MouseLocation mouseLocation = new MouseLocation();
		this.getGraphicalControl().addMouseListener(mouseLocation);

		// Create the context menu
		ContextMenuProvider menuProvider = new ContextMenuProvider(this.getGraphicalViewer()) {
			@Override
			public void buildContextMenu(IMenuManager menuManager) {

				// Obtain the location
				Point location = mouseLocation.getLocation();

				// Obtain the selected edit parts
				List<EditPart> selectedEditPartList = new LinkedList<EditPart>();
				ISelection selection = AbstractOfficeFloorEditor.this.getGraphicalViewer().getSelection();
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
					Object selectedItem = iterator.next();

					// Obtain the edit part and add to listing
					EditPart editPart = (EditPart) selectedItem;
					selectedEditPartList.add(editPart);
				}
				EditPart[] selectedEditParts = selectedEditPartList.toArray(new EditPart[0]);

				// Ensure have selected edit parts
				if (selectedEditParts.length == 0) {
					// Nothing selected, therefore add no actions
					return;
				}

				// Add the appropriate actions
				for (Operation operation : AbstractOfficeFloorEditor.this.operations) {

					// Determine if handles all edit part types selected
					boolean isHandled = operation.isApplicable(selectedEditParts);

					// Add if handles all model types
					if (isHandled) {
						// Add action for the commands to the menu
						menuManager.add(new OperationAction(AbstractOfficeFloorEditor.this.getCommandStack(), operation,
								selectedEditParts, location));
					}
				}
			}
		};
		Menu menu = menuProvider.createContextMenu(this.getGraphicalControl());

		// Register the context menu
		this.getGraphicalControl().setMenu(menu);
	}

	/**
	 * Populates the listing of {@link Operation} instances.
	 * 
	 * @param list
	 *            Listing to add {@link Operation} instances.
	 */
	protected abstract void populateOperations(List<Operation> list);

	/**
	 * Use the model type to {@link EditPart} map to create the appropriate
	 * {@link EditPart} via its default constructor.
	 */
	@SuppressWarnings("unchecked")
	public EditPart createEditPart(EditPart context, Object model) {

		// Ensure have a model
		if (model == null) {
			this.messageError("No model");
			return null;
		}

		// Obtain the edit part type for the model
		Class<EditPart> editPartType = (Class<EditPart>) this.modelTypeToEditPartTypeMap.get(model.getClass());

		// Ensure have type
		if (editPartType == null) {
			this.messageError("No EditPart for model " + model.getClass().getSimpleName());
			return null;
		}

		// Create the instance of the edit part
		EditPart editPart = EclipseUtil.createInstance(editPartType, this);
		if (editPart == null) {
			this.messageError("Failed to obtain EditPart for model " + model.getClass().getSimpleName());
			return null;
		}

		// Load in the model
		editPart.setModel(model);

		// Enrich the edit part
		if (editPart instanceof AbstractOfficeFloorEditPart) {
			AbstractOfficeFloorEditPart<?, ?, ?> officeFloorEditPart = (AbstractOfficeFloorEditPart<?, ?, ?>) editPart;
			officeFloorEditPart.setOfficeFloorEditor(this);
		} else if (editPart instanceof AbstractOfficeFloorConnectionEditPart) {
			AbstractOfficeFloorConnectionEditPart<?, ?> officeFloorConnectionEditPart = (AbstractOfficeFloorConnectionEditPart<?, ?>) editPart;
			officeFloorConnectionEditPart.setOfficeFloorEditor(this);
		}

		// Return the edit part
		return editPart;
	}

	/**
	 * Allows sub classes to trigger the population of the {@link EditPart}
	 * types.
	 */
	protected void loadEditPartTypes() {
		this.populateEditPartTypes(this.modelTypeToEditPartTypeMap);
	}

	/**
	 * Populates the {@link EditPart} types for their respective model.
	 * 
	 * @param map
	 *            Registry to load the mappings.
	 */
	protected abstract void populateEditPartTypes(Map<Class<?>, Class<? extends EditPart>> map);

	/*
	 * ================== EditorPart ====================================
	 */

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Obtain the configuration
		WritableConfigurationItem configuration = ProjectConfigurationContext
				.getWritableConfigurationItem(this.getEditorInput(), null);

		// Retrieve the Model
		try {
			this.setCastedModel(this.retrieveModel(configuration));
		} catch (Exception ex) {
			this.messageError(ex);
			return;
		}

		// Specify Title of editor
		IFile file = ProjectConfigurationContext.getFile(this.getEditorInput());
		this.setPartName(file.getName());

		// Create the model changes
		this.modelChanges = this.createModelChanges(this.getCastedModel());
	}

	/**
	 * Creates the {@link Model} change functionality.
	 * 
	 * @param model
	 *            Root {@link Model}.
	 * @return {@link Model} change functionality.
	 */
	protected abstract C createModelChanges(M model);

	/**
	 * Retrieves the Model.
	 * 
	 * @param configuration
	 *            Configuration of the Model.
	 * @return Model to be edited.
	 * @throws Exception
	 *             If fails to obtain the Model.
	 */
	protected abstract M retrieveModel(ConfigurationItem configuration) throws Exception;

	/*
	 * ==================== EditorPart ====================================
	 */

	@Override
	public void doSave(IProgressMonitor monitor) {

		// Obtain the configuration
		WritableConfigurationItem configuration = ProjectConfigurationContext
				.getWritableConfigurationItem(this.getEditorInput(), monitor);

		try {
			// Store the Model
			this.storeModel(this.getCastedModel(), configuration);

			// Successfully saved, so flag not dirty
			this.getCommandStack().markSaveLocation();

		} catch (Exception ex) {
			this.messageError(ex);
		}
	}

	/**
	 * Stores the Model.
	 * 
	 * @param model
	 *            Model to be stored.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the Model.
	 */
	protected abstract void storeModel(M model, WritableConfigurationItem configuration) throws Exception;

	@Override
	public void doSaveAs() {
		// Provide empty method implementation.
	}

	@Override
	public boolean isSaveAsAllowed() {
		// Always able to save
		return true;
	}

	@Override
	protected FlyoutPreferences getPalettePreferences() {
		return new CommonFlyoutPreferences();
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		// Create the root (if not created)
		if (this.paletteRoot == null) {
			this.paletteRoot = new PaletteRoot();

			// Add the selection tools
			PaletteGroup selectionGroup = new PaletteGroup("Select");
			selectionGroup.add(new SelectionToolEntry());
			paletteRoot.add(selectionGroup);

			// Add the connection group
			PaletteGroup connectionGroup = new PaletteGroup("Connection");
			connectionGroup.add(new ConnectionCreationToolEntry("Connection", "conn", new CreationFactory() {

				public Object getNewObject() {
					// Connection determined in command
					return null;
				}

				public Object getObjectType() {
					// Connection determined in command
					return null;
				}
			}, null, null));
			paletteRoot.add(connectionGroup);

			// Initialise remaining palette root
			this.initialisePaletteRoot();
		}

		// Return the Root
		return paletteRoot;
	}

	/**
	 * Override to initialise the {@link PaletteRoot}.
	 * 
	 * @see #getPaletteRoot()
	 */
	protected void initialisePaletteRoot() {
		// Do nothing
	}

}