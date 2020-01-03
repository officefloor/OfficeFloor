package net.officefloor.compile.impl.managedfunction;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedFunctionType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionTypeImpl<M extends Enum<M>, F extends Enum<F>>
		implements ManagedFunctionType<M, F>, ManagedFunctionTypeBuilder<M, F> {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<M, F> functionFactory;

	/**
	 * {@link Enum} providing keys for dependent {@link Object} instances.
	 */
	private final Class<M> objectKeyClass;

	/**
	 * {@link ManagedFunctionObjectType} instances.
	 */
	private final List<ManagedFunctionObjectType<M>> objects = new LinkedList<ManagedFunctionObjectType<M>>();

	/**
	 * {@link Enum} providing keys for instigated {@link Flow} instances.
	 */
	private final Class<F> flowKeyClass;

	/**
	 * {@link ManagedFunctionFlowType} instances.
	 */
	private final List<ManagedFunctionFlowType<F>> flows = new LinkedList<ManagedFunctionFlowType<F>>();

	/**
	 * {@link ManagedFunctionEscalationType} instances.
	 */
	private final List<ManagedFunctionEscalationType> escalations = new LinkedList<ManagedFunctionEscalationType>();

	/**
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

	/**
	 * Return type.
	 */
	private Class<?> returnType = null;

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param objectKeyClass
	 *            {@link Enum} providing keys for dependent {@link Object}
	 *            instances.
	 * @param flowKeyClass
	 *            {@link Enum} providing keys for instigated {@link Flow} instances.
	 */
	public ManagedFunctionTypeImpl(String functionName, ManagedFunctionFactory<M, F> functionFactory,
			Class<M> objectKeyClass, Class<F> flowKeyClass) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
		this.objectKeyClass = objectKeyClass;
		this.flowKeyClass = flowKeyClass;
	}

	/*
	 * ==================== ManagedFunctionTypeBuilder =========================
	 */

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
	}

	@Override
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	@Override
	public ManagedFunctionObjectTypeBuilder<M> addObject(Class<?> objectType) {
		ManagedFunctionObjectTypeImpl<M> object = new ManagedFunctionObjectTypeImpl<M>(objectType, this.objects.size());
		this.objects.add(object);
		return object;
	}

	@Override
	public ManagedFunctionFlowTypeBuilder<F> addFlow() {
		ManagedFunctionFlowTypeImpl<F> flow = new ManagedFunctionFlowTypeImpl<F>(this.flows.size());
		this.flows.add(flow);
		return flow;
	}

	@Override
	public <E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType) {
		ManagedFunctionEscalationTypeImpl escalation = new ManagedFunctionEscalationTypeImpl(escalationType);
		this.escalations.add(escalation);
		return escalation;
	}

	/*
	 * ======================= ManagedFunctionType ==========================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public ManagedFunctionFactory<M, F> getManagedFunctionFactory() {
		return this.functionFactory;
	}

	@Override
	public Object[] getAnnotations() {
		return this.annotations.toArray(new Object[this.annotations.size()]);
	}

	@Override
	public Class<M> getObjectKeyClass() {
		return this.objectKeyClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ManagedFunctionObjectType<M>[] getObjectTypes() {
		return this.objects.stream().sorted((a, b) -> a.getIndex() - b.getIndex())
				.toArray(ManagedFunctionObjectType[]::new);
	}

	@Override
	public Class<F> getFlowKeyClass() {
		return this.flowKeyClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ManagedFunctionFlowType<F>[] getFlowTypes() {
		return this.flows.stream().sorted((a, b) -> a.getIndex() - b.getIndex())
				.toArray(ManagedFunctionFlowType[]::new);
	}

	@Override
	public ManagedFunctionEscalationType[] getEscalationTypes() {
		return this.escalations.toArray(new ManagedFunctionEscalationType[0]);
	}

	@Override
	public Class<?> getReturnType() {
		return this.returnType;
	}

}