package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link AdministrationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationTypeImpl<E, F extends Enum<F>, G extends Enum<G>> implements AdministrationType<E, F, G> {

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<E, F, G> administrationFactory;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link Flow} key {@link Enum}.
	 */
	private final Class<F> flowKeyClass;

	/**
	 * {@link AdministrationFlowType} instances.
	 */
	private final AdministrationFlowType<F>[] flows;

	/**
	 * {@link AdministrationEscalationType} instances.
	 */
	private final AdministrationEscalationType[] escalations;

	/**
	 * {@link Governance} key {@link Enum}.
	 */
	private final Class<G> governanceKeyClass;

	/**
	 * {@link AdministrationGovernanceType} instances.
	 */
	private final AdministrationGovernanceType<G>[] governances;

	/**
	 * Initiate.
	 * 
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param flowKeyClass
	 *            {@link Flow} key {@link Enum}.
	 * @param flows
	 *            {@link AdministrationFlowType} instances.
	 * @param escalations
	 *            {@link AdministrationEscalationType} instances.
	 * @param governanceKeyClass
	 *            {@link Governance} key {@link Enum}.
	 * @param governances
	 *            {@link AdministrationGovernanceType} instances.
	 */
	public AdministrationTypeImpl(AdministrationFactory<E, F, G> administrationFactory, Class<E> extensionInterface,
			Class<F> flowKeyClass, AdministrationFlowType<F>[] flows, AdministrationEscalationType[] escalations,
			Class<G> governanceKeyClass, AdministrationGovernanceType<G>[] governances) {
		this.administrationFactory = administrationFactory;
		this.extensionInterface = extensionInterface;
		this.flowKeyClass = flowKeyClass;
		this.flows = flows;
		this.escalations = escalations;
		this.governanceKeyClass = governanceKeyClass;
		this.governances = governances;
	}

	/*
	 * ==================== AdministrationType ================================
	 */

	@Override
	public Class<E> getExtensionType() {
		return this.extensionInterface;
	}

	@Override
	public AdministrationFactory<E, F, G> getAdministrationFactory() {
		return this.administrationFactory;
	}

	@Override
	public Class<F> getFlowKeyClass() {
		return this.flowKeyClass;
	}

	@Override
	public AdministrationFlowType<F>[] getFlowTypes() {
		return flows;
	}

	@Override
	public AdministrationEscalationType[] getEscalationTypes() {
		return this.escalations;
	}

	@Override
	public Class<G> getGovernanceKeyClass() {
		return this.governanceKeyClass;
	}

	@Override
	public AdministrationGovernanceType<G>[] getGovernanceTypes() {
		return this.governances;
	}

}