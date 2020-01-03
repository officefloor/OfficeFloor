package net.officefloor.compile.internal.structure;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for loading a type.
 *
 * @author Daniel Sagenschneider
 */
public interface CompileContext {

	/**
	 * Registers a possible MBean.
	 *
	 * @param <T>
	 *            Type of MBean.
	 * @param <S>
	 *            MBean type.
	 * @param type
	 *            Type of MBean.
	 * @param name
	 *            Name of MBean.
	 * @param mbean
	 *            MBean.
	 */
	<T, S extends T> void registerPossibleMBean(Class<T> type, String name, S mbean);

	/**
	 * Obtains the existing or loads the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} to obtain the
	 *            {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> with issue reported to
	 *         the {@link CompilerIssues}.
	 */
	ManagedObjectType<?> getOrLoadManagedObjectType(ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Obtains the existing or loads the {@link FunctionNamespaceType} for the
	 * {@link FunctionNamespaceNode}.
	 * 
	 * @param functionNamespaceNode
	 *            {@link FunctionNamespaceNode} to obtain the
	 *            {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	FunctionNamespaceType getOrLoadFunctionNamespaceType(FunctionNamespaceNode functionNamespaceNode);

	/**
	 * Obtains the existing or loads the {@link SuppliedManagedObjectSourceType} for
	 * the {@link SuppliedManagedObjectSourceNode}.
	 * 
	 * @param suppliedManagedObjectSourceNode
	 *            {@link SuppliedManagedObjectSourceNode} to obtain the
	 *            {@link SuppliedManagedObjectSourceType}.
	 * @return {@link SuppliedManagedObjectSourceType} or <code>null</code> with
	 *         issue reported to the {@link CompilerIssues}.
	 */
	SuppliedManagedObjectSourceType getOrLoadSuppliedManagedObjectSourceType(
			SuppliedManagedObjectSourceNode suppliedManagedObjectSourceNode);

	/**
	 * Obtains the existing or loads the {@link SupplierType} for the
	 * {@link SupplierNode}.
	 * 
	 * @param supplierNode
	 *            {@link SupplierNode} to obtain the {@link SupplierType}.
	 * @return {@link SupplierType} or <code>null</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	SupplierType getOrLoadSupplierType(SupplierNode supplierNode);

	/**
	 * Obtains the existing or loads the {@link TeamType} for the {@link TeamNode}.
	 * 
	 * @param teamNode
	 *            {@link TeamNode} to obtain the {@link TeamType}.
	 * @return {@link TeamType} or <code>null</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	TeamType getOrLoadTeamType(TeamNode teamNode);

	/**
	 * Obtains the existing or loads the {@link AdministrationType} for the
	 * {@link AdministrationNode}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            {@link Flow} key {@link Enum} type.
	 * @param <G>
	 *            {@link Governance} key {@link Enum} type.
	 * @param administrationNode
	 *            {@link AdministrationNode} to obtain the
	 *            {@link AdministrationType}.
	 * @return {@link AdministrationType} or <code>null</code> with issue reported
	 *         to the {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>> AdministrationType<E, F, G> getOrLoadAdministrationType(
			AdministrationNode administrationNode);

	/**
	 * Obtains the existing or loads the {@link GovernanceType} for the
	 * {@link GovernanceNode}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <F>
	 *            Flow key {@link Enum} type.
	 * @param governanceNode
	 *            {@link GovernanceNode} to obtain the {@link GovernanceType}.
	 * @return {@link GovernanceType} or <code>null</code> with issue report to the
	 *         {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>> GovernanceType<E, F> getOrLoadGovernanceType(GovernanceNode governanceNode);

}