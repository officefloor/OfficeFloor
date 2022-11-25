package net.officefloor.cabinet.domain;

import java.lang.reflect.Method;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * {@link Method} implementation of domain {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodImplementation {

	Object invoke(CabinetManager cabinetManager, Object[] arguments) throws Exception;
}