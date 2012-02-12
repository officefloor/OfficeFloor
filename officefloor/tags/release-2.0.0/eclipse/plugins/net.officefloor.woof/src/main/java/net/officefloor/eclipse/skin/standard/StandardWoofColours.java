/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
 * {@link Color} values for the {@link StandardWoofSkin}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardWoofColours {

	/**
	 * Black (#000000).
	 * 
	 * @return Black.
	 */
	public static Color BLACK() {
		return new Color(null, 0, 0, 0);
	}

	/**
	 * Render complete template output.
	 * 
	 * @return Render complete template output.
	 */
	public static Color RENDER_COMPLETE_TEMPLATE_OUTPUT() {
		return new Color(null, 240, 240, 240);
	}

	/**
	 * Template (#bbe0ff).
	 * 
	 * @return Template.
	 */
	public static Color TEMPLATE() {
		return new Color(null, 187, 224, 255);
	}

	/**
	 * Section (#b1e8b1);
	 * 
	 * @return Section.
	 */
	public static Color SECTION() {
		return new Color(null, 177, 232, 177);
	}

	/**
	 * Governance
	 * 
	 * @return Governance.
	 */
	public static Color GOVERNANCE() {
		return new Color(null, 245, 245, 96);
	}

	/**
	 * Resource (#e5e5e5).
	 * 
	 * @return Resource.
	 */
	public static Color RESOURCE() {
		return new Color(null, 229, 229, 229);
	}

	/**
	 * Connector (#000000).
	 * 
	 * @return Connector.
	 */
	public static Color CONNECTOR() {
		return new Color(null, 0, 0, 0);
	}

}