/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;

/**
 * <p>
 * Stateless {@link SessionBean} along with utility methods for integrating
 * {@link OfficeFloor} into an Application Server.
 * <p>
 * The deployment descriptor for using this {@link SessionBean} is as follows
 * (replacing the contents of the square brackets - [...]):
 * <p>
 * 
 * <pre>
 * &lt;ejb-jar&gt;
 *     ...
 *     &lt;enterprise-beans&gt;
 *         ...
 *         &lt;session&gt;
 *             &lt;ejb-name&gt;[EJB Name]&lt;/ejb-name&gt;
 *             &lt;ejb-class&gt;net.officefloor.plugin.ejb.OfficeFloorEjb&lt;/ejb-class&gt;
 *             &lt;env-entry&gt;
 *                 &lt;env-entry-name&gt;officeFloorJndiName&lt;/env-entry-name&gt;
 *                 &lt;env-entry-value&gt;[JNDI resource path to OfficeFloor configuration]&lt;/env-entry-value&gt;
 *             &lt;/env-entry&gt;
 *             &lt;env-entry&gt;
 *                 &lt;env-entry-name&gt;officeName&lt;/env-entry-name&gt;
 *                 &lt;env-entry-value&gt;[Name of Office]&lt;/env-entry-value&gt;
 *             &lt;/env-entry&gt;
 *             &lt;env-entry&gt;
 *                 &lt;env-entry-name&gt;workName&lt;/env-entry-name&gt;
 *                 &lt;env-entry-value&gt;[Name of Work]&lt;/env-entry-value&gt;
 *             &lt;/env-entry&gt;
 *         &lt;/session&gt;
 *         ...
 *     &lt;/enterprise-beans&gt;
 *     ...
 * &lt;/ejb-jar&gt;
 * </pre>
 * 
 * <p>
 * The {@link EjbOrchestrator} and {@link EjbOrchestratorRemote} provide a
 * generic interface for orchestration. It is also possible to extend this class
 * to provide a typed interface:
 * <p>
 * Local interface:
 * 
 * <pre>
 * &#064;Local
 * public interface TypedEjbOrchestrator {
 * 	public void typedOrchestration(String parameter) throws NamingException;
 * }
 * </pre>
 * 
 * <p>
 * Bean implementation:
 * 
 * <pre>
 * &#064;Stateless
 * public class TypedOfficeFloorEjb extends OfficeFloorEjb implements
 * 		TypedEjbOrchestrator {
 * 	public void typedOrchestration(String parameter) throws NamingException {
 * 		this.orchestrate(parameter);
 * 	}
 * }
 * </pre>
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OfficeFloorEjb implements EjbOrchestrator, EjbOrchestratorRemote {

	/**
	 * Does the orchestration.
	 * 
	 * @param officeFloorJndiName
	 *            {@link OfficeFloor} JNDI name.
	 * @param officeName
	 *            Name of the {@link Office} within the {@link OfficeFloor}.
	 * @param workName
	 *            Name of {@link Work} within the {@link Office}.
	 * @param parameter
	 *            Parameter for the {@link Work}.
	 * @throws NamingException
	 *             If fails to instigate the orchestration.
	 */
	public static void doOrchestration(String officeFloorJndiName,
			String officeName, String workName, Object parameter)
			throws NamingException {

		// Obtain the Initial Context
		Context initialContext = new InitialContext();

		// Lookup the OfficeFloor
		Object object = initialContext.lookup(officeFloorJndiName);
		if ((object == null) || (!(object instanceof OfficeFloor))) {
			throw new NamingException("Lookup by jndi-name '"
					+ officeFloorJndiName
					+ "' did not find a "
					+ OfficeFloor.class.getSimpleName()
					+ (object == null ? "" : " (Looked up " + object + " ["
							+ object.getClass().getName() + "])"));
		}
		OfficeFloor officeFloor = (OfficeFloor) object;

		try {
			// Obtain the Work Manager
			Office office = officeFloor.getOffice(officeName);
			WorkManager workManager = office.getWorkManager(workName);

			// Do the work
			ProcessContextTeam.doWork(workManager, parameter);

		} catch (InterruptedException ex) {
			return;

		} catch (UnknownOfficeException ex) {
			throw new NamingException(ex.getMessage());
		} catch (UnknownWorkException ex) {
			throw new NamingException(ex.getMessage());
		} catch (NoInitialTaskException ex) {
			throw new NamingException(ex.getMessage());
		} catch (InvalidParameterTypeException ex) {
			throw new NamingException(ex.getMessage());
		}
	}

	/**
	 * JDNI name for the {@link OfficeFloor}.
	 */
	@Resource(name = "officeFloorJndiName")
	protected String officeFloorJndiName;

	/**
	 * Name of the {@link Office} within the {@link OfficeFloor}.
	 */
	@Resource(name = "officeName")
	protected String officeName;

	/**
	 * Name of the {@link Work} to be invoked.
	 */
	@Resource(name = "workName")
	protected String workName;

	/**
	 * Validates the dependency injection of configuration available.
	 * 
	 * @throws EJBException
	 *             If configuration is not dependency injected.
	 */
	@PostConstruct
	public void ejbCreate() throws EJBException {
		assertNotBlank(this.officeFloorJndiName, "officeFloorJndiName");
		assertNotBlank(this.officeName, "officeName");
		assertNotBlank(this.workName, "workName");
	}

	/**
	 * Asserts the dependency is not blank.
	 * 
	 * @param dependency
	 *            Dependency.
	 * @param name
	 *            Name of the dependency value.
	 * @throws EJBException
	 *             If dependency is blank.
	 */
	private static void assertNotBlank(String dependency, String name)
			throws EJBException {
		if ((dependency == null) || (dependency.trim().length() == 0)) {
			throw new EJBException("env-entry for name '" + name
					+ "' must be provided");
		}
	}

	/*
	 * ================== EjbOrchestratorRemote ====================
	 */

	@Override
	public <P> P orchestrateRemotely(P parameter) throws NamingException {

		// Do the orchestration
		this.orchestrate(parameter);

		// Return the potentially altered parameter
		return parameter;
	}

	/*
	 * ====================== EjbOrchestrator =======================
	 */

	@Override
	public void orchestrate(Object parameter) throws NamingException {
		doOrchestration(this.officeFloorJndiName, this.officeName,
				this.workName, parameter);
	}

}