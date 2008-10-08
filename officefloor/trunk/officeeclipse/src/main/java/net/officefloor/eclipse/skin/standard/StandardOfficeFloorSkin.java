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
package net.officefloor.eclipse.skin.standard;

import net.officefloor.eclipse.skin.OfficeFloorSkin;
import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.room.RoomFigureFactory;

/**
 * The standard {@link OfficeFloorSkin}.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorSkin implements OfficeFloorSkin {

	/**
	 * {@link DeskFigureFactory}.
	 */
	private final DeskFigureFactory deskFigureFactory = new StandardDeskFigureFactory();

	/**
	 * {@link OfficeFigureFactory}.
	 */
	private final OfficeFigureFactory officeFigureFactory = new StandardOfficeFigureFactory();

	/**
	 * {@link OfficeFloorFigureFactory}.
	 */
	private final OfficeFloorFigureFactory officeFloorFigureFactory = new StandardOfficeFloorFigureFactory();

	/**
	 * {@link RoomFigureFactory}.
	 */
	private final RoomFigureFactory roomFigureFactory = new StandardRoomFigureFactory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorSkin#getDeskFigureFactory()
	 */
	@Override
	public DeskFigureFactory getDeskFigureFactory() {
		return this.deskFigureFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.OfficeFloorSkin#getOfficeFigureFactory()
	 */
	@Override
	public OfficeFigureFactory getOfficeFigureFactory() {
		return this.officeFigureFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.OfficeFloorSkin#getOfficeFloorFigureFactory
	 * ()
	 */
	@Override
	public OfficeFloorFigureFactory getOfficeFloorFigureFactory() {
		return this.officeFloorFigureFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorSkin#getRoomFigureFactory()
	 */
	@Override
	public RoomFigureFactory getRoomFigureFactory() {
		return this.roomFigureFactory;
	}

}
