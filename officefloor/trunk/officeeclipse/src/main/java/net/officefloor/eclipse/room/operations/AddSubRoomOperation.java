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
package net.officefloor.eclipse.room.operations;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.filter.FileExtensionInputFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.common.dialog.input.translator.ResourceFullPathValueTranslator;
import net.officefloor.eclipse.room.editparts.RoomEditPart;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.room.RoomLoader;

import org.eclipse.core.resources.IFile;

/**
 * Adds a {@link SubRoomModel} to the {@link RoomModel}.
 * 
 * @author Daniel
 */
public class AddSubRoomOperation extends AbstractOperation<RoomEditPart> {

	/**
	 * Initiate.
	 */
	public AddSubRoomOperation() {
		super("Add sub room", RoomEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the room edit part
		final RoomEditPart editPart = context.getEditPart();

		// Add the Sub Room
		SubRoomAddBean bean = new SubRoomAddBean();
		BeanDialog dialog = editPart.createBeanDialog(bean);
		dialog.registerPropertyInput("File", new ClasspathSelectionInput(
				editPart.getEditor(), new ClasspathFilter(IFile.class,
						new FileExtensionInputFilter("desk", "room"))));
		dialog.registerPropertyValueTranslator("File",
				new ResourceFullPathValueTranslator());
		SubRoomModel subRoom = null;
		if (dialog.populate()) {
			try {
				// Obtain the class path location
				String classPathLocation = ClasspathUtil
						.getClassPathLocation(bean.file);

				// Obtain the configuration item
				ConfigurationItem configItem = ProjectClassLoader
						.findConfigurationItem(editPart.getEditor(),
								classPathLocation);
				if (configItem == null) {
					editPart.messageError("Can not find '" + classPathLocation
							+ "' on class path");
					return;
				}

				// Create the sub room
				RoomLoader roomLoader = new RoomLoader();
				subRoom = roomLoader.loadSubRoom(configItem);
				subRoom.setId(bean.name);

			} catch (Exception ex) {
				editPart.messageError(ex);
			}
		}

		// Ensure have the sub room
		if (subRoom == null) {
			return;
		}

		// Set location
		context.positionModel(subRoom);

		// Make the change
		final SubRoomModel newSubRoom = subRoom;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Add the sub room
				editPart.getCastedModel().addSubRoom(newSubRoom);
			}

			@Override
			protected void undoCommand() {
				// Remove the sub room
				editPart.getCastedModel().removeSubRoom(newSubRoom);
			}
		});
	}

	/**
	 * Object to add a {@link SubRoomModel}.
	 */
	public static class SubRoomAddBean {

		/**
		 * Name of the {@link SubRoomModel}.
		 */
		public String name;

		/**
		 * Name of the file.
		 */
		public String file;

		/**
		 * Specify the name.
		 * 
		 * @param name
		 *            Name of the {@link SubRoomModel}.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Specify the file name.
		 * 
		 * @param file
		 *            File name.
		 */
		public void setFile(String file) {
			this.file = file;
		}
	}

}
