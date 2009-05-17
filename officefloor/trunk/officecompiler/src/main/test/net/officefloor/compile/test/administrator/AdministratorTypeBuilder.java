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
package net.officefloor.compile.test.administrator;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Builder of the {@link AdministratorType} to validate the loaded
 * {@link AdministratorType} from the {@link AdministratorSource}.
 * 
 * @author Daniel
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