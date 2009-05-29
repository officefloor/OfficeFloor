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
package net.officefloor.frame.spi.administration.source;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Describes a {@link Flow} required by a {@link Duty} of the
 * {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorDutyFlowMetaData<F extends Enum<F>> {

	/**
	 * Obtains the {@link Enum} key identifying this {@link Flow}. If
	 * <code>null</code> then {@link Flow} will be referenced by this instance's
	 * index in the array returned from {@link AdministratorDutyMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Flow} or
	 *         <code>null</code> indicating identified by an index.
	 */
	F getKey();

	/**
	 * <p>
	 * Obtains the {@link Class} of the argument that is passed to the
	 * {@link Flow}.
	 * <p>
	 * This may be <code>null</code> to indicate no argument is passed.
	 * 
	 * @return Type of the argument that is passed to the {@link Flow}.
	 */
	Class<?> getArgumentType();

	/**
	 * Provides a descriptive name for this {@link Flow}. This is useful to
	 * better describe the {@link Flow}.
	 * 
	 * @return Descriptive name for this {@link Flow}.
	 */
	String getLabel();

}