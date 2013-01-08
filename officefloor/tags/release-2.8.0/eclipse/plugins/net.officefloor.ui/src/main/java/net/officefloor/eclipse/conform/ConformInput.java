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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.conform.editparts.ConformEditPart;
import net.officefloor.eclipse.conform.editparts.ExistingItemEditPart;
import net.officefloor.eclipse.conform.editparts.ExistingItemToTargetItemEditPart;
import net.officefloor.eclipse.conform.editparts.ExistingModelEditPart;
import net.officefloor.eclipse.conform.editparts.TargetItemEditPart;
import net.officefloor.eclipse.conform.editparts.TargetModelEditPart;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.conform.ExistingItemModel;
import net.officefloor.model.conform.ExistingItemToTargetItemModel;
import net.officefloor.model.conform.ExistingModel;
import net.officefloor.model.conform.TargetItemModel;
import net.officefloor.model.conform.TargetModel;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.widgets.Canvas;

/**
 * {@link Input} using the {@link GraphicalViewerImpl} to populate the
 * {@link ConformModel} links between the old and the new.
 * 
 * @author Daniel Sagenschneider
 */
public class ConformInput implements Input<Canvas> {

	/**
	 * {@link ConformModel}.
	 */
	private ConformModel conform;

	/**
	 * {@link GraphicalViewerImpl}.
	 */
	private GraphicalViewer viewer;

	/**
	 * Convenience method to load the {@link ConformModel} based on the names
	 * and provide default mappings.
	 * 
	 * @param existingItemNames
	 *            Names for the {@link ExistingItemModel} instances.
	 * @param targetItemNames
	 *            Names for he {@link TargetItemModel} instances.
	 */
	public void setConform(String[] existingItemNames, String[] targetItemNames) {

		// Create the conform model for the tasks
		ConformModel conform = new ConformModel();

		// Adding existing model
		ExistingModel existing = new ExistingModel();
		conform.setExistingModel(existing);
		for (String existingItemName : existingItemNames) {
			existing.addExistingItem(new ExistingItemModel(existingItemName));
		}

		// Add target model
		TargetModel target = new TargetModel();
		conform.setTargetModel(target);
		for (String targetItemName : targetItemNames) {
			target.addTargetItem(new TargetItemModel(targetItemName));
		}

		// Specify the conform model
		this.setConform(conform);

		// Add the default mapping by name
		this.addMappingsByName();
	}

	/**
	 * Obtains the {@link ConformModel} as a mapping of {@link TargetItemModel}
	 * name to {@link ExistingItemModel} name.
	 * 
	 * @return Mapping of {@link TargetItemModel} name to
	 *         {@link ExistingItemModel} name.
	 */
	public Map<String, String> getTargetItemToExistingItemMapping() {
		// Create the map and populate the map
		Map<String, String> map = new HashMap<String, String>();
		if (this.conform != null) {
			TargetModel target = this.conform.getTargetModel();
			if (target != null) {
				for (TargetItemModel targetItem : target.getTargetItems()) {
					ExistingItemToTargetItemModel conn = targetItem
							.getExistingItem();
					if (conn != null) {
						ExistingItemModel existingItem = conn.getExistingItem();
						if (existingItem != null) {
							// Add the mapping of target to existing
							map.put(targetItem.getTargetItemName(),
									existingItem.getExistingItemName());
						}
					}
				}
			}
		}

		// Return the map
		return map;
	}

	/**
	 * Specifies the (or change of) {@link ConformModel}.
	 * 
	 * @param conform
	 *            {@link ConformModel}.
	 */
	public void setConform(ConformModel conform) {
		this.conform = conform;

		// Provide locations
		ExistingModel existing = this.conform.getExistingModel();
		if (existing != null) {
			existing.setX(10);
			existing.setY(10);
		}
		TargetModel target = this.conform.getTargetModel();
		if (target != null) {
			target.setX(300);
			target.setY(10);
		}

		// Provide to viewer (if built)
		if (this.viewer != null) {
			this.viewer.setContents(this.conform);
		}
	}

	/**
	 * Adds in the mappings between {@link ExistingItemModel} and
	 * {@link TargetItemModel} instances by name.
	 */
	public void addMappingsByName() {

		// Ensure have conform
		if (this.conform == null) {
			return;
		}

		// Create the mapping of existing items
		Map<String, ExistingItemModel> existingItems = new HashMap<String, ExistingItemModel>();
		ExistingModel existingModel = this.conform.getExistingModel();
		if (existingModel != null) {
			for (ExistingItemModel existingItem : existingModel
					.getExistingItems()) {
				existingItems.put(existingItem.getExistingItemName(),
						existingItem);
			}
		}

		// Connect the target items to existing items by name
		TargetModel targetModel = this.conform.getTargetModel();
		if (targetModel != null) {
			for (TargetItemModel targetItem : targetModel.getTargetItems()) {

				// Obtain the corresponding existing item
				String itemName = targetItem.getTargetItemName();
				ExistingItemModel existingItem = existingItems.get(itemName);
				if (existingItem == null) {
					continue; // no corresponding existing item
				}

				// Connect target item to existing item
				new ExistingItemToTargetItemModel(existingItem, targetItem)
						.connect();

				// Remove existing item so only connect once
				existingItems.remove(itemName);
			}
		}
	}

	/*
	 * ==================== Input ===============================
	 */

	@Override
	public Canvas buildControl(InputContext context) {

		// Create the editor
		RootEditPart rootEditPart = new ScalableFreeformRootEditPart();
		ConformEditor editor = new ConformEditor(rootEditPart, context
				.getParent().getShell());

		// Provide the model types to edit part types
		editor.mapModelToEditPart(ConformModel.class, ConformEditPart.class);
		editor.mapModelToEditPart(ExistingModel.class,
				ExistingModelEditPart.class);
		editor.mapModelToEditPart(ExistingItemModel.class,
				ExistingItemEditPart.class);
		editor.mapModelToEditPart(TargetModel.class, TargetModelEditPart.class);
		editor.mapModelToEditPart(TargetItemModel.class,
				TargetItemEditPart.class);
		editor.mapModelToEditPart(ExistingItemToTargetItemModel.class,
				ExistingItemToTargetItemEditPart.class);

		// Create the control to display figures
		this.viewer = new GraphicalViewerImpl();
		Canvas canvas = (Canvas) this.viewer.createControl(context.getParent());
		this.viewer.setRootEditPart(rootEditPart);
		this.viewer.setEditPartFactory(editor);

		// Create the edit domain (to allow connecting items)
		EditDomain editDomain = new EditDomain();
		editDomain.addViewer(this.viewer);
		editDomain.setActiveTool(new ConnectionCreationTool());

		// Provide contents (if available)
		if (this.conform != null) {
			this.viewer.setContents(this.conform);
		}

		// Return the contents containing the canvas
		return canvas;
	}

	@Override
	public Object getValue(Canvas control, InputContext context) {
		return this.conform;
	}

}