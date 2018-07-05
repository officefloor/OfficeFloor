/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * Describes a {@link Escalation} from the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationEscalationMetaData {

	/**
	 * Obtains the {@link Class} of the {@link Escalation}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return {@link Class} of the {@link Escalation}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

	/**
	 * Provides a descriptive name for this {@link Escalation}. This is useful to
	 * better describe the {@link Escalation}.
	 * 
	 * @return Descriptive name for this {@link Escalation}.
	 */
	String getLabel();

}