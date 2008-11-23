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
package net.officefloor.eclipse.officefloor.operations;

import org.eclipse.core.resources.IFile;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.filter.FileExtensionInputFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.common.dialog.input.translator.ResourceFullPathValueTranslator;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.officefloor.OfficeFloorLoader;
import net.officefloor.repository.ConfigurationItem;

/**
 * Adds an {@link OfficeFloorOfficeModel} to a {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class AddOfficeOperation extends AbstractOperation<OfficeFloorEditPart> {

	/**
	 * Initiate.
	 */
	public AddOfficeOperation() {
		super("Add office", OfficeFloorEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the edit part
		final OfficeFloorEditPart editPart = context.getEditPart();

		// Create the Office
		OfficeFloorOfficeModel office = null;
		OfficeFloorOfficeModel officeBean = new OfficeFloorOfficeModel();
		BeanDialog dialog = editPart.createBeanDialog(officeBean, "X", "Y");
		dialog.registerPropertyInput("Id", new ClasspathSelectionInput(editPart
				.getEditor(), new ClasspathFilter(IFile.class,
				new FileExtensionInputFilter("office"))));
		dialog.registerPropertyValueTranslator("Id",
				new ResourceFullPathValueTranslator());
		if (dialog.populate()) {
			try {
				// Obtain the class path location
				String classPathLocation = ClasspathUtil
						.getClassPathLocation(officeBean.getId());

				// Obtain the office configuration
				ProjectClassLoader classLoader = ProjectClassLoader
						.create(editPart.getEditor());
				ConfigurationItem officeConfigItem = classLoader
						.findConfigurationItem(classPathLocation);
				if (officeConfigItem == null) {
					editPart.messageError("Could not find Office at '"
							+ classPathLocation + "'");
					return;
				}

				// Load the Office
				OfficeFloorLoader officeFloorLoader = new OfficeFloorLoader();
				office = officeFloorLoader
						.loadOfficeFloorOffice(officeConfigItem);

				// Specify name of office
				office.setName(officeBean.getName());

			} catch (Exception ex) {
				editPart.messageError(ex);
			}
		}

		// Ensure created office
		if (office == null) {
			return;
		}

		// Set location
		context.positionModel(office);

		// Add the office
		final OfficeFloorOfficeModel newOffice = office;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addOffice(newOffice);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeOffice(newOffice);
			}
		});
	}

}
