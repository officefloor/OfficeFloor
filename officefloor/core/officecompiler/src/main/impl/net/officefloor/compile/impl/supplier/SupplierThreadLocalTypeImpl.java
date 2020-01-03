package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * {@link SupplierThreadLocalType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierThreadLocalTypeImpl<T> implements SupplierThreadLocalType {

	/**
	 * {@link SupplierThreadLocal} instances for this
	 * {@link SupplierThreadLocalType}.
	 */
	private final SupplierThreadLocalImpl<T> supplierThreadLocal = new SupplierThreadLocalImpl<>();

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type.
	 */
	public SupplierThreadLocalTypeImpl(String qualifier, Class<?> objectType) {
		this.qualifier = qualifier;
		this.objectType = objectType;
	}

	/**
	 * Obtains the {@link SupplierThreadLocal}.
	 * 
	 * @return {@link SupplierThreadLocal}.
	 */
	public SupplierThreadLocal<T> getSupplierThreadLocal() {
		return (SupplierThreadLocal<T>) this.supplierThreadLocal;
	}

	/*
	 * =================== SupplierThreadLocalType =====================
	 */

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	/*
	 * ================= OptionalThreadLocalReceiver ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void setOptionalThreadLocal(OptionalThreadLocal<?> optionalThreadLocal) {
		this.supplierThreadLocal.optionalThreadLocal = (OptionalThreadLocal<T>) optionalThreadLocal;
	}

	/**
	 * {@link SupplierThreadLocal} implementation.
	 */
	public static class SupplierThreadLocalImpl<T> implements SupplierThreadLocal<T> {

		/**
		 * {@link OptionalThreadLocal} to retrieve the {@link ManagedObject} object.
		 */
		private OptionalThreadLocal<T> optionalThreadLocal = null;

		/*
		 * ================= SupplierThreadLocal =======================
		 */

		@Override
		public T get() {
			return this.optionalThreadLocal.get();
		}
	}

}