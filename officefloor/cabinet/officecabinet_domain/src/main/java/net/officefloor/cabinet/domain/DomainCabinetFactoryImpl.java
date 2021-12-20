package net.officefloor.cabinet.domain;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.cabinet.admin.OfficeCabinetAdmin;

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
	public C createDomainSpecificCabinet(CabinetSession session) {
		return (C) Proxy.newProxyInstance(this.classLoader, new Class[] { this.cabinetType, OfficeCabinetAdmin.class },
				new CabinetInvocationHandler(session));
	}

	@Override
	public DomainCabinetDocumentMetaData[] getMetaData() {
		return this.metaData;
	}

	private class CabinetInvocationHandler implements InvocationHandler {

		private final CabinetSession session;

		private CabinetInvocationHandler(CabinetSession session) {
			this.session = session;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			MethodImplementation methodImplementation = DomainCabinetFactoryImpl.this.methodImplementations
					.get(method.getName());
			return methodImplementation.invoke(this.session, args);
		}
	}

}