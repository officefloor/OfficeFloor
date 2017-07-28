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
package net.officefloor.compile.test.governance;

import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder of the {@link GovernanceType} to validate the loaded
 * {@link GovernanceType} from the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceTypeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the extension interface type.
	 * 
	 * @param extensionInterface
	 *            Extension interface type.
	 */
	void setExtensionInterface(Class<?> extensionInterface);

	/**
	 * Adds a {@link GovernanceFlowType}.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

	/**
	 * Adds an {@link Escalation}.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 */
	void addEscalation(Class<?> escalationType);

}