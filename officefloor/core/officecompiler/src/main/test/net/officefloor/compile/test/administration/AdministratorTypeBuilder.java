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
package net.officefloor.compile.test.administration;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.administration.DutyType;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder of the {@link AdministrationType} to validate the loaded
 * {@link AdministrationType} from the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorTypeBuilder {

	/**
	 * Specifies the extension interface type.
	 * 
	 * @param extensionInterface
	 *            Extension interface type.
	 */
	void setExtensionInterface(Class<?> extensionInterface);

	/**
	 * Adds a {@link DutyType}.
	 * 
	 * @param dutyName
	 *            Name of the {@link Duty}.
	 * @param dutyKey
	 *            Key of the {@link Duty}.
	 * @return {@link DutyTypeBuilder}.
	 */
	DutyTypeBuilder<Indexed> addDuty(String dutyName, Enum<?> dutyKey);

	/**
	 * Adds a {@link DutyType}.
	 * 
	 * @param <F>
	 *            {@link Flow} type keys.
	 * @param dutyName
	 *            Name of the {@link Duty}.
	 * @param dutyKey
	 *            Key of the {@link Duty}.
	 * @param flowKeyClass
	 *            {@link Flow} key class.
	 * @return {@link DutyTypeBuilder}.
	 */
	<F extends Enum<F>> DutyTypeBuilder<F> addDuty(String dutyName,
			Enum<?> dutyKey, Class<F> flowKeyClass);

}