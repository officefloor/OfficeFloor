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
package net.officefloor.eclipse.common.dialog;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p>
 * Builder to create the {@link org.eclipse.swt.widgets.Control} for inputting
 * the value of the property.
 * <p>
 * It also provides means to obtain the String value from the resulting built
 * {@link org.eclipse.swt.widgets.Control}.
 * 
 * @author Daniel
 */
public interface PropertyInput<C extends Control> {

	/**
	 * Builds the {@link Control}.
	 * 
	 * @param initialValue
	 *            Initial value for the property.
	 * @param listener
	 *            To listen to changes of the property's value.
	 * @param parent
	 *            Parent {@link Composite} to add the {@link Control}.
	 * @return {@link Control} to input the value of the property.
	 */
	C buildControl(Object initialValue, VerifyListener listener,
			Composite parent);

	/**
	 * <p>
	 * Obtains the String value from the input {@link Control}.
	 * <p>
	 * The input {@link Control} will be the one created by
	 * {@link #buildControl(Object, VerifyListener, Composite)}.
	 * 
	 * @param control
	 *            {@link Control}.
	 * @param e
	 *            {@link VerifyEvent} given from the input {@link Control}.
	 * @return String value.
	 */
	String getValue(C control, VerifyEvent e);

}
