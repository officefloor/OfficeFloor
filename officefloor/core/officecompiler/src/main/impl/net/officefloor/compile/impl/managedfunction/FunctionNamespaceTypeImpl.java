package net.officefloor.compile.impl.managedfunction;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link FunctionNamespaceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceTypeImpl implements FunctionNamespaceType, FunctionNamespaceBuilder {

	/**
	 * Listing of the {@link ManagedFunctionType} definitions.
	 */
	private final List<ManagedFunctionType<?, ?>> functions = new LinkedList<ManagedFunctionType<?, ?>>();

	/*
	 * =================== FunctionNamespaceBuilder ===================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addManagedFunctionType(
			String taskName, ManagedFunctionFactory<M, F> functionFactory, Class<M> objectKeysClass,
			Class<F> flowKeysClass) {
		ManagedFunctionTypeImpl functionType = new ManagedFunctionTypeImpl(taskName, functionFactory, objectKeysClass,
				flowKeysClass);
		this.functions.add(functionType);
		return functionType;
	}

	/*
	 * =================== FunctionNamespaceType ===================
	 */

	@Override
	public ManagedFunctionType<?, ?>[] getManagedFunctionTypes() {
		return this.functions.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getFunctionName(), b.getFunctionName()))
				.toArray(ManagedFunctionType[]::new);
	}

}