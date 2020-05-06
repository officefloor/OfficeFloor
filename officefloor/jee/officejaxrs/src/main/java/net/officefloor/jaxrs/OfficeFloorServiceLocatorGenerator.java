package net.officefloor.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MethodParameter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorState;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ServiceLocatorGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServiceLocatorGenerator implements ServiceLocatorGenerator, ServiceLocator {

	/*
	 * ================== ServiceLocatorGenerator ================
	 */

	@Override
	public ServiceLocator create(String name, ServiceLocator parent) {
		return this;
	}

	/*
	 * ======================= ServiceLocator =====================
	 */

	@Override
	public <T> T getService(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 1 " + contractOrImpl.getName());
	}

	@Override
	public <T> T getService(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 2");
	}

	@Override
	public <T> T getService(Class<T> contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 3");
	}

	@Override
	public <T> T getService(Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 4");
	}

	@Override
	public <T> List<T> getAllServices(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getAllServices
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServices 5");
	}

	@Override
	public <T> List<T> getAllServices(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getAllServices
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServices 6");
	}

	@Override
	public <T> List<T> getAllServices(Annotation qualifier, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getAllServices
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServices 7");
	}

	@Override
	public List<?> getAllServices(Filter searchCriteria) throws MultiException {
		// TODO implement ServiceLocator.getAllServices
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServices 8");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 9");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 10");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, String name, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 11");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, String name, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 12");
	}

	@Override
	public <T> List<ServiceHandle<T>> getAllServiceHandles(Class<T> contractOrImpl, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getAllServiceHandles
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServiceHandles 13");
	}

	@Override
	public List<ServiceHandle<?>> getAllServiceHandles(Type contractOrImpl, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getAllServiceHandles
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServiceHandles 14");
	}

	@Override
	public List<ServiceHandle<?>> getAllServiceHandles(Annotation qualifier, Annotation... qualifiers)
			throws MultiException {
		// TODO implement ServiceLocator.getAllServiceHandles
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServiceHandles 15");
	}

	@Override
	public List<ServiceHandle<?>> getAllServiceHandles(Filter searchCriteria) throws MultiException {
		// TODO implement ServiceLocator.getAllServiceHandles
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getAllServiceHandles 16");
	}

	@Override
	public List<ActiveDescriptor<?>> getDescriptors(Filter filter) {
		// TODO implement ServiceLocator.getDescriptors
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getDescriptors 17");
	}

	@Override
	public ActiveDescriptor<?> getBestDescriptor(Filter filter) {
		// TODO implement ServiceLocator.getBestDescriptor
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getBestDescriptor 18");
	}

	@Override
	public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor, Injectee injectee) throws MultiException {
		// TODO implement ServiceLocator.reifyDescriptor
		throw new UnsupportedOperationException("TODO implement ServiceLocator.reifyDescriptor 19");
	}

	@Override
	public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor) throws MultiException {
		// TODO implement ServiceLocator.reifyDescriptor
		throw new UnsupportedOperationException("TODO implement ServiceLocator.reifyDescriptor 20");
	}

	@Override
	public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee) throws MultiException {
		// TODO implement ServiceLocator.getInjecteeDescriptor
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getInjecteeDescriptor 21");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor, Injectee injectee)
			throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 22");
	}

	@Override
	public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor) throws MultiException {
		// TODO implement ServiceLocator.getServiceHandle
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getServiceHandle 23");
	}

	@Override
	public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 24");
	}

	@Override
	public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root, Injectee injectee)
			throws MultiException {
		// TODO implement ServiceLocator.getService
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getService 25");
	}

	@Override
	public String getDefaultClassAnalyzerName() {
		// TODO implement ServiceLocator.getDefaultClassAnalyzerName
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getDefaultClassAnalyzerName 26");
	}

	@Override
	public void setDefaultClassAnalyzerName(String defaultClassAnalyzer) {
		// TODO implement ServiceLocator.setDefaultClassAnalyzerName
		throw new UnsupportedOperationException("TODO implement ServiceLocator.setDefaultClassAnalyzerName 27");
	}

	@Override
	public Unqualified getDefaultUnqualified() {
		// TODO implement ServiceLocator.getDefaultUnqualified
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getDefaultUnqualified 28");
	}

	@Override
	public void setDefaultUnqualified(Unqualified unqualified) {
		// TODO implement ServiceLocator.setDefaultUnqualified
		throw new UnsupportedOperationException("TODO implement ServiceLocator.setDefaultUnqualified 29");
	}

	@Override
	public String getName() {
		// TODO implement ServiceLocator.getName
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getName 30");
	}

	@Override
	public long getLocatorId() {
		// TODO implement ServiceLocator.getLocatorId
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getLocatorId 31");
	}

	@Override
	public ServiceLocator getParent() {
		// TODO implement ServiceLocator.getParent
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getParent 32");
	}

	@Override
	public void shutdown() {
		// TODO implement ServiceLocator.shutdown
		throw new UnsupportedOperationException("TODO implement ServiceLocator.shutdown 33");
	}

	@Override
	public ServiceLocatorState getState() {
		// TODO implement ServiceLocator.getState
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getState 34");
	}

	@Override
	public boolean getNeutralContextClassLoader() {
		// TODO implement ServiceLocator.getNeutralContextClassLoader
		throw new UnsupportedOperationException("TODO implement ServiceLocator.getNeutralContextClassLoader 35");
	}

	@Override
	public void setNeutralContextClassLoader(boolean neutralContextClassLoader) {
		System.out.println("setNeutralContextClassLoader " + neutralContextClassLoader);
	}

	@Override
	public <T> T create(Class<T> createMe) {
		// TODO implement ServiceLocator.create
		throw new UnsupportedOperationException("TODO implement ServiceLocator.create 36");
	}

	@Override
	public <T> T create(Class<T> createMe, String strategy) {
		// TODO implement ServiceLocator.create
		throw new UnsupportedOperationException("TODO implement ServiceLocator.create 37");
	}

	@Override
	public void inject(Object injectMe) {
		// TODO implement ServiceLocator.inject
		throw new UnsupportedOperationException("TODO implement ServiceLocator.inject 38");
	}

	@Override
	public void inject(Object injectMe, String strategy) {
		// TODO implement ServiceLocator.inject
		throw new UnsupportedOperationException("TODO implement ServiceLocator.inject 39");
	}

	@Override
	public Object assistedInject(Object injectMe, Method method, MethodParameter... params) {
		// TODO implement ServiceLocator.assistedInject
		throw new UnsupportedOperationException("TODO implement ServiceLocator.assistedInject 40");
	}

	@Override
	public Object assistedInject(Object injectMe, Method method, ServiceHandle<?> root, MethodParameter... params) {
		// TODO implement ServiceLocator.assistedInject
		throw new UnsupportedOperationException("TODO implement ServiceLocator.assistedInject 41");
	}

	@Override
	public void postConstruct(Object postConstructMe) {
		// TODO implement ServiceLocator.postConstruct
		throw new UnsupportedOperationException("TODO implement ServiceLocator.postConstruct 42");
	}

	@Override
	public void postConstruct(Object postConstructMe, String strategy) {
		// TODO implement ServiceLocator.postConstruct
		throw new UnsupportedOperationException("TODO implement ServiceLocator.postConstruct 43");
	}

	@Override
	public void preDestroy(Object preDestroyMe) {
		// TODO implement ServiceLocator.preDestroy
		throw new UnsupportedOperationException("TODO implement ServiceLocator.preDestroy 44");
	}

	@Override
	public void preDestroy(Object preDestroyMe, String strategy) {
		// TODO implement ServiceLocator.preDestroy
		throw new UnsupportedOperationException("TODO implement ServiceLocator.preDestroy 45");
	}

	@Override
	public <U> U createAndInitialize(Class<U> createMe) {
		// TODO implement ServiceLocator.createAndInitialize
		throw new UnsupportedOperationException("TODO implement ServiceLocator.createAndInitialize 46");
	}

	@Override
	public <U> U createAndInitialize(Class<U> createMe, String strategy) {
		// TODO implement ServiceLocator.createAndInitialize
		throw new UnsupportedOperationException("TODO implement ServiceLocator.createAndInitialize 47");
	}

}