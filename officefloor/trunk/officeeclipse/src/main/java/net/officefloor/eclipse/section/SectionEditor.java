/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.section;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.section.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.section.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionInputEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionObjectEditPart;
import net.officefloor.eclipse.section.editparts.SubSectionOutputEditPart;
import net.officefloor.eclipse.section.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.section.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.section.operations.AddSubSectionOperation;
import net.officefloor.eclipse.section.operations.RefreshSubSectionOperation;
import net.officefloor.eclipse.section.operations.ToggleSubSectionInputPublicOperation;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link SectionModel}.
 * 
 * @author Daniel
 */
public class SectionEditor extends
		AbstractOfficeFloorEditor<SectionModel, SectionEditPart> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.section";

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
		map
				.put(SubSectionObjectModel.class,
						SubSectionObjectEditPart.class);

		// Connections
		map.put(SubSectionObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(SubSectionOutputToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(SubSectionOutputToSubSectionInputModel.class,
				OfficeFloorConnectionEditPart.class);
	}

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
	protected void populateOperations(List<Operation> list) {
		// Add model add operations
		list.add(new AddSubSectionOperation());
		list.add(new AddExternalManagedObjectOperation());
		list.add(new AddExternalFlowOperation());

		// Add refresh operations
		list.add(new RefreshSubSectionOperation());

		// Sub room input flow operations
		list.add(new ToggleSubSectionInputPublicOperation());
	}

}