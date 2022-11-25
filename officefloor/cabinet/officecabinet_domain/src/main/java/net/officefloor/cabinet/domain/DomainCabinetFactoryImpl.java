package net.officefloor.cabinet.domain;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.spi.CabinetManager;

/**
 * {@link DomainCabinetFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainCabinetFactoryImpl<C> implements DomainCabinetFactory<C> {

	private final Class<C> cabinetType;

	private final Map<String, MethodImplementation> methodImplementations;

	private final DomainCabinetDocumentMetaData[] metaData;

	private final ClassLoader classLoader;

	public DomainCabinetFactoryImpl(Class<C> cabinetType, Map<String, MethodImplementation> methodImplementations,
			DomainCabinetDocumentMetaData[] metaData, ClassLoader classLoader) {
		this.cabinetType = cabinetType;
		this.methodImplementations = methodImplementations;
		this.metaData = metaData;
		this.classLoader = classLoader;
	}

	/*
	 * ========================= DomainCabinetFactory ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public C createDomainSpecificCabinet(CabinetManager cabinetManager) {
		return (C) Proxy.newProxyInstance(this.classLoader, new Class[] { this.cabinetType, OfficeCabinetAdmin.class },
				new CabinetInvocationHandler(cabinetManager));
	}

	@Override
	public DomainCabinetDocumentMetaData[] getMetaData() {
		return this.metaData;
	}

	private class CabinetInvocationHandler implements InvocationHandler {

		private final CabinetManager cabinetManager;

		private CabinetInvocationHandler(CabinetManager cabinetManager) {
			this.cabinetManager = cabinetManager;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			MethodImplementation methodImplementation = DomainCabinetFactoryImpl.this.methodImplementations
					.get(method.getName());
			return methodImplementation.invoke(this.cabinetManager, args);
		}
	}

}