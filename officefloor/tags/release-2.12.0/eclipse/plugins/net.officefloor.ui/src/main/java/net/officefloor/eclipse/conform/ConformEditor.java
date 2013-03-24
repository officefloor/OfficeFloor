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
package net.officefloor.eclipse.conform;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.conform.ExistingItemModel;
import net.officefloor.model.conform.ExistingItemToTargetItemModel;
import net.officefloor.model.conform.TargetItemModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link AbstractOfficeFloorEditor} for a {@link Dialog} {@link Control}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConformEditor extends AbstractOfficeFloorEditor<Model, Object> {

	/**
	 * {@link RootEditPart}.
	 */
	private final RootEditPart rootEditPart;

	/**
	 * {@link Shell}.
	 */
	private final Shell shell;

	/**
	 * Map of {@link Model} type to {@link EditPart} type.
	 */
	private Map<Class<?>, Class<? extends EditPart>> modelToEditPartMap;

	/**
	 * Initialise.
	 * 
	 * @param rootEditPart
	 *            {@link RootEditPart}.
	 * @param shell
	 *            {@link Shell}.
	 */
	public ConformEditor(RootEditPart rootEditPart, Shell shell) {
		this.rootEditPart = rootEditPart;
		this.shell = shell;

		// Trigger load edit part types to obtain the map
		this.loadEditPartTypes();
	}

	/**
	 * Maps a {@link Model} type to its respective {@link EditPart} type.
	 * 
	 * @param modelType
	 *            {@link Model} type.
	 * @param editPartType
	 *            {@link EditPart} type.
	 */
	public void mapModelToEditPart(Class<?> modelType,
			Class<? extends EditPart> editPartType) {
		this.modelToEditPartMap.put(modelType, editPartType);
	}

	/*
	 * ===== overridden methods to provide necessary functionality ============
	 */

	@Override
	public RootEditPart getRootEditPart() {
		return this.rootEditPart;
	}

	@Override
	public void messageStatus(IStatus status, String title) {
		ErrorDialog.openError(this.shell, title, null, status);
	}

	/*
	 * ================= AbstractOfficeFloorEditor ========================
	 */

	@Override
	protected Object createModelChanges(Model model) {
		return new Object();
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {
		// Keep reference to allow population
		this.modelToEditPartMap = map;
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect existing item to target item
		policy.addConnection(
				ExistingItemModel.class,
				TargetItemModel.class,
				new ConnectionChangeFactory<ExistingItemModel, TargetItemModel>() {
					@Override
					public Change<?> createChange(
							final ExistingItemModel source,
							final TargetItemModel target,
							CreateConnectionRequest request) {

						// Determine if target item is inheriting
						if (target.getInherit()) {
							return null; // inheriting so no configuration
						}

						// Create the connection
						final ExistingItemToTargetItemModel conn = new ExistingItemToTargetItemModel(
								source, target);

						// Return change to connect source to target
						return new AbstractChange<ExistingItemToTargetItemModel>(
								conn, "Connect") {
							@Override
							public void apply() {

								// Remove possible existing connection
								if (source.getTargetItem() != null) {
									source.getTargetItem().remove();
								}

								// Remove possible target connection
								if (target.getExistingItem() != null) {
									target.getExistingItem().remove();
								}

								// Add the new connection
								conn.connect();
							}

							@Override
							public void revert() {
								// Can not revert
							}
						};
					}
				});
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {
		// Not used
	}

	@Override
	protected void populateOperations(List<Operation> list) {
		// Not used
	}

	@Override
	protected Model retrieveModel(ConfigurationItem configuration)
			throws Exception {
		// Not used
		return null;
	}

	@Override
	protected void storeModel(Model model, ConfigurationItem configuration)
			throws Exception {
		// Not used
	}

}