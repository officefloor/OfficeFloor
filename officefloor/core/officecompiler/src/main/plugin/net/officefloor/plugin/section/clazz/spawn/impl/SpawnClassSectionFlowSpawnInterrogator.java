/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.section.clazz.spawn.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Spawn;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogator;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorContext;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorServiceFactory;

/**
 * {@link ClassSectionFlowSpawnInterrogator} for {@link Spawn}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnClassSectionFlowSpawnInterrogator
		implements ClassSectionFlowSpawnInterrogator, ClassSectionFlowSpawnInterrogatorServiceFactory {

	/*
	 * ============= ClassSectionFlowSpawnInterrogatorServiceFactory =============
	 */

	@Override
	public ClassSectionFlowSpawnInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ClassSectionFlowSpawnInterrogator ====================
	 */

	@Override
	public boolean isSpawnFlow(ClassSectionFlowSpawnInterrogatorContext context) throws Exception {

		// Determine if have spawn annotation
		Spawn spawn = context.getManagedFunctionFlowType().getAnnotation(Spawn.class);
		return spawn != null;
	}

}
