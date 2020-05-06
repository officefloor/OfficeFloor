package net.officefloor.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionResolverBinding;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.inject.SupplierClassBinding;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link InjectionManagerFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorInjectionManagerFactory implements InjectionManagerFactory, InjectionManager {

	/**
	 * {@link Map} of contract {@link Class} to its {@link Supplier}.
	 */
	private final Map<Type, Supplier<?>> dependencyFactories = new HashMap<>();

	private <T> T createInstance(Class<? extends T> clazz) {
		try {
			return (T) clazz.getConstructor().newInstance();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * =================== InjectionManagerFactory ==================
	 */

	@Override
	public InjectionManager create(Object parent) {
		return this;
	}

	/**
	 * ======================= InjectionManager ======================
	 */

	@Override
	public void completeRegistration() {
		System.out.println("completeRegistrion");
	}

	@Override
	public void shutdown() {
		// TODO implement InjectionManager.shutdown
		throw new UnsupportedOperationException("TODO implement InjectionManager.shutdown");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void register(Binding binding) {
		System.out.println("TODO implement InjectionManager.register - " + binding.getClass() + " : "
				+ binding.getName() + " : " + binding.getAliases() + " : " + binding.getImplementationType() + " : "
				+ binding.getContracts() + " : " + binding.getQualifiers() + " : " + binding.getAnalyzer() + " : "
				+ binding.getScope() + " : ");
		if (binding instanceof InjectionResolverBinding) {
			System.out.println("RESOLVE: " + ((InjectionResolverBinding) binding).getResolver().getClass().getName());
		}

		// Register the binding
		if (binding instanceof SupplierClassBinding) {
			SupplierClassBinding<Object> supplierClassBinding = (SupplierClassBinding<Object>) binding;
			Supplier<?> supplier = this.createInstance(supplierClassBinding.getSupplierClass());
			for (Type contract : supplierClassBinding.getContracts()) {
				this.dependencyFactories.put(contract, supplier);
			}
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void register(Iterable<Binding> descriptors) {
		for (Binding<?, ?> binding : descriptors) {
			this.register(binding);
		}
	}

	@Override
	public void register(Binder binder) {
		this.register(binder.getBindings());
	}

	@Override
	public void register(Object provider) throws IllegalArgumentException {
		System.out.println("register (provider) " + provider.getClass());
		Binder binder = (Binder) provider;
		this.register(binder);
	}

	@Override
	public boolean isRegistrable(Class<?> clazz) {

		System.out.println("isRegistrable " + clazz.getName());
		return Binder.class.isAssignableFrom(clazz);
	}

	@Override
	public <T> T createAndInitialize(Class<T> createMe) {
		System.out.println("createAndInitialize " + createMe.getName());
		return this.createInstance(createMe);
	}

	@Override
	public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
		// TODO implement InjectionManager.getAllServiceHolders
		throw new UnsupportedOperationException("TODO implement InjectionManager.getAllServiceHolders");
	}

	@Override
	public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
		// TODO implement InjectionManager.getInstance
		throw new UnsupportedOperationException("TODO implement InjectionManager.getInstance 1");
	}

	@Override
	public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
		// TODO implement InjectionManager.getInstance
		throw new UnsupportedOperationException("TODO implement InjectionManager.getInstance 2");
	}

	@Override
	public <T> T getInstance(Class<T> contractOrImpl) {

		System.out.println("getInstance " + contractOrImpl.getName());
		
		// Obtain the factory
		Supplier<?> supplier = this.dependencyFactories.get(contractOrImpl);
		if (supplier != null) {
			System.out.println("  Found supplier " + supplier.getClass().getName());
			return (T) supplier.get();
		}
		
		return this.createAndInitialize(contractOrImpl);
	}

	@Override
	public <T> T getInstance(Type contractOrImpl) {
		// TODO implement InjectionManager.getInstance
		throw new UnsupportedOperationException("TODO implement InjectionManager.getInstance 4");
	}

	@Override
	public Object getInstance(ForeignDescriptor foreignDescriptor) {
		// TODO implement InjectionManager.getInstance
		throw new UnsupportedOperationException("TODO implement InjectionManager.getInstance 5");
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ForeignDescriptor createForeignDescriptor(Binding binding) {
		// TODO implement InjectionManager.createForeignDescriptor
		throw new UnsupportedOperationException("TODO implement InjectionManager.createForeignDescriptor");
	}

	@Override
	public <T> List<T> getAllInstances(Type contractOrImpl) {
		// TODO implement InjectionManager.getAllInstances
		throw new UnsupportedOperationException("TODO implement InjectionManager.getAllInstances");
	}

	@Override
	public void inject(Object injectMe) {
		// TODO implement InjectionManager.inject
		throw new UnsupportedOperationException("TODO implement InjectionManager.inject");
	}

	@Override
	public void inject(Object injectMe, String classAnalyzer) {
		// TODO implement InjectionManager.inject
		throw new UnsupportedOperationException("TODO implement InjectionManager.inject");
	}

	@Override
	public void preDestroy(Object preDestroyMe) {
		// TODO implement InjectionManager.preDestroy
		throw new UnsupportedOperationException("TODO implement InjectionManager.preDestroy");
	}

}