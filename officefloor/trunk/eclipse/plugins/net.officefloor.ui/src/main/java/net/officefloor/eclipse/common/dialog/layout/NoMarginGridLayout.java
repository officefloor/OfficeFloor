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
package net.officefloor.eclipse.common.dialog.layout;

import org.eclipse.swt.layout.GridLayout;

/**
 * No spacing {@link GridLayout}.
 * 
 * @author Daniel Sagenschneider
 */
public class NoMarginGridLayout {

	/**
	 * As per {@link GridLayout} constructor.
	 * 
	 * @return {@link GridLayout}.
	 */
	public static GridLayout create() {
		return removeMargins(new GridLayout());
	}

	/**
	 * As per {@link GridLayout} constructor.
	 * 
	 * @param numOfColumns
	 *            Number of columns.
	 * @param makeColumnsEqualWidth
	 *            Make columns equal width.
	 * @return {@link GridLayout}.
	 */
	public static GridLayout create(int numOfColumns,
			boolean makeColumnsEqualWidth) {
		return removeMargins(new GridLayout(numOfColumns, makeColumnsEqualWidth));
	}

	/**
	 * Removes the margins from the {@link GridLayout}.
	 * 
	 * @param layout
	 *            {@link GridLayout}.
	 * @return Input {@link GridLayout} for easier coding.
	 */
	private static GridLayout removeMargins(GridLayout layout) {
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginWidth = 0;
		return layout;
	}

	/**
	 * Only via static methods to create {@link GridLayout}.
	 */
	private NoMarginGridLayout() {
	}
}