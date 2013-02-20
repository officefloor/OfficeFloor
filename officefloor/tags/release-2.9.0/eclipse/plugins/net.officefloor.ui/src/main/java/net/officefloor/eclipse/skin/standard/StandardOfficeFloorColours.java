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
package net.officefloor.eclipse.skin.standard;

import org.eclipse.swt.graphics.Color;

/**
 * {@link Color} values for {@link StandardOfficeFloorSkin}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorColours {

	/**
	 * Black (#000000).
	 *
	 * @return Black.
	 */
	public static Color BLACK() {
		return new Color(null, 0, 0, 0);
	}

	/**
	 * Error (#ff0000).
	 *
	 * @return Error.
	 */
	public static Color ERROR() {
		return new Color(null, 255, 0, 0);
	}

	/**
	 * Link line (#c0c0c0).
	 *
	 * @return Link line.
	 */
	public static Color LINK_LINE() {
		return new Color(null, 192, 192, 192);
	}

	/**
	 * Line ({@link #BLACK()}).
	 *
	 * @return Line.
	 */
	public static Color LINE() {
		return BLACK();
	}

	/**
	 * Initial task line ({@link #TASK()}).
	 *
	 * @return Initial task line.
	 */
	public static Color INITIAL_TASK_LINE() {
		return TASK();
	}

	/**
	 * External object (#b1e8b1);
	 *
	 * @return External object.
	 */
	public static Color EXTERNAL_OBJECT() {
		return new Color(null, 177, 232, 177);
	}

	/**
	 * Task (#bbe0ff).
	 *
	 * @return Task.
	 */
	public static Color TASK() {
		return new Color(null, 187, 224, 255);
	}

	/**
	 * Administrator (#a9aed1).
	 *
	 * @return Administrator.
	 */
	public static Color ADMINISTRATOR() {
		return new Color(null, 169, 174, 209);
	}

	/**
	 * Section ({@link #TASK()}).
	 *
	 * @return Section.
	 */
	public static Color SECTION() {
		return TASK();
	}

	/**
	 * Office ({@link #TASK()}).
	 *
	 * @return Office.
	 */
	public static Color OFFICE() {
		return TASK();
	}

	/**
	 * Managed Object ({@link #EXTERNAL_OBJECT()}).
	 *
	 * @return Managed Object.
	 */
	public static Color MANAGED_OBJECT() {
		return EXTERNAL_OBJECT();
	}

	/**
	 * Input Managed Object ({@link #EXTERNAL_OBJECT()}).
	 *
	 * @return Input Managed Object.
	 */
	public static Color INPUT_MANAGED_OBJECT() {
		return EXTERNAL_OBJECT();
	}

	/**
	 * Managed Object Source (#e5e5e5).
	 *
	 * @return Managed Object Source.
	 */
	public static Color MANAGED_OBJECT_SOURCE() {
		return new Color(null, 229, 229, 229);
	}

	/**
	 * Sub section ({@link #SECTION()}).
	 *
	 * @return Sub section.
	 */
	public static Color SUB_SECTION() {
		return SECTION();
	}
}