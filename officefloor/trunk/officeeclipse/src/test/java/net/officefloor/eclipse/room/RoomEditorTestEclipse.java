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
package net.officefloor.eclipse.room;

import net.officefloor.eclipse.AbstractEditorTest;
import net.officefloor.eclipse.EditorTestSpecific;

/**
 * Tests the {@link net.officefloor.eclipse.room.RoomEditor}.
 * 
 * @author Daniel
 */
public class RoomEditorTestEclipse extends AbstractEditorTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.AbstractEditorTest#getEditorTestSpecific()
	 */
	protected EditorTestSpecific getEditorTestSpecific() {
		return new EditorTestSpecific("net.officefloor.editors.room",
				"TestRoom.room.xml", "TestRoom.room.xml");
	}

	/**
	 * Open the {@link RoomEditor}.
	 */
	public void testOpenRoomEditor() {
		// Flag Room Editor as dirty so will not exit
		RoomEditor roomEditor = (RoomEditor) this.getEditorPart();
		roomEditor.flagDirty();
	}

}
