/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
