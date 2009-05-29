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
package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of a possible {@link EscalationFlow} by the
 * {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskEscalationTypeBuilder {

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link EscalationFlow}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link EscalationFlow}. If not set it will use the <code>Simple</code>
	 * name of the {@link EscalationFlow} {@link Class}.
	 * 
	 * @param label
	 *            Display label for the {@link EscalationFlow}.
	 */
	void setLabel(String label);

}