package net.officefloor.compile.impl.managedfunction;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;

/**
 * {@link ManagedFunctionObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectTypeImpl<M extends Enum<M>>
		implements ManagedFunctionObjectType<M>, ManagedFunctionObjectTypeBuilder<M> {

	/**
	 * Type of the dependency {@link Object}.
	 */
	private final Class<?> objectType;

	/**
	 * Type qualifier.
	 */
	private String typeQualifier;

	/**
	 * Label describing this {@link ManagedFunctionObjectType}.
	 */
	private String label = null;

	/**
	 * Index identifying this {@link ManagedFunctionObjectType}.
	 */
	private int index;

	/**
	 * {@link Enum} key identifying this {@link ManagedFunctionObjectType}.
	 */
	private M key = null;

	/**
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

	/**
	 * Initiate with the index of the {@link ManagedFunctionObjectType}.
	 * 
	 * @param objectType
	 *            Type of the dependency {@link Object}.
	 * @param index
	 *            Index identifying this {@link ManagedFunctionObjectType}.
	 */
	public ManagedFunctionObjectTypeImpl(Class<?> objectType, int index) {
		this.objectType = objectType;
		this.index = index;
	}

	/*
	 * ================= ManagedFunctionObjectTypeBuilder =================
	 */

	@Override
	public void setKey(M key) {
		this.key = key;
		this.index = key.ordinal();
	}

	@Override
	public void setTypeQualifier(String qualifier) {
		this.typeQualifier = qualifier;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
	}

	/*
	 * ================== ManagedFunctionObjectType ==================
	 */

	@Override
	public String getObjectName() {
		// Follow priorities to obtain the object name
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.key != null) {
			return this.key.toString();
		} else {
			return String.valueOf(this.index);
		}
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public M getKey() {
		return this.key;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations.toArray(new Object[this.annotations.size()]);
	}

}