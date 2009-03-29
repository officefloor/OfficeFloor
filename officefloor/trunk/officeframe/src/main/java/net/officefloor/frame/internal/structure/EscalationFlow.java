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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link Flow} to handle an {@link Escalation} from a {@link JobNode}.
 * 
 * @author Daniel
 */
public interface EscalationFlow {

	/**
	 * Obtains the type of cause handled by this {@link EscalationFlow}.
	 * 
	 * @return Type of cause handled by this {@link EscalationFlow}.
	 */
	Class<? extends Throwable> getTypeOfCause();

	/**
	 * Obtains the {@link FlowMetaData} of the escalation handling.
	 * 
	 * @return {@link FlowMetaData} of the escalation handling.
	 */
	FlowMetaData<?> getFlowMetaData();

}