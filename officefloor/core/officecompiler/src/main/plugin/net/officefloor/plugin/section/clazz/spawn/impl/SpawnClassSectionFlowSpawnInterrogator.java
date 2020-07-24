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