/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.dialog.input;

import org.eclipse.swt.widgets.Control;

/**
 * <p>
 * Builder to create the {@link Control} for inputting the value.
 * <p>
 * It also provides means to obtain the value from the resulting built
 * {@link Control}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Input<C extends Control> {

	/**
	 * Builds the {@link Control}.
	 * 
	 * @param context
	 *            {@link InputContext}.
	 * @return {@link Control} to input the value.
	 */
	C buildControl(InputContext context);

	/**
	 * <p>
	 * Obtains the value from the input {@link Control}.
	 * <p>
	 * The input {@link Control} will be the one created by
	 * {@link #buildControl(InputContext)}.
	 * 
	 * @param control
	 *            {@link Control}.
	 * @param context
	 *            {@link InputContext}.
	 * @return Value.
	 */
	Object getValue(C control, InputContext context);

}
