package net.officefloor.frame.api.team.source;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;

/**
 * Context for the {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamSourceContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the name of the {@link Team} to be created from the
	 * {@link TeamSource}.
	 * <p>
	 * This enables naming the {@link Thread} instances for the {@link Team} to be
	 * specific to the {@link Team}.
	 * 
	 * @return Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	String getTeamName();

	/**
	 * <p>
	 * Obtains the size of the {@link Team}.
	 * <p>
	 * Typically this is the maximum number of {@link Thread} instances for the
	 * {@link Team}. However, for some {@link Team} implementations it may not be
	 * used (e.g. {@link PassiveTeamSource}).
	 * <p>
	 * It is provided to allow the {@link Executive} to have some control over
	 * {@link Team} sizes.
	 * 
	 * @return {@link Team} size.
	 */
	int getTeamSize();

	/**
	 * Allows obtaining the size of the {@link Team}, without forcing it to be
	 * configured.
	 * 
	 * @param defaultSize Default size of the {@link Team}, if no size configured.
	 * @return {@link Team} size.
	 */
	int getTeamSize(int defaultSize);

	/**
	 * <p>
	 * Obtains the {@link ThreadFactory} for the {@link Team}.
	 * <p>
	 * It is encouraged for {@link Team} implementations to use this in creating
	 * {@link Thread} instances. This is to enable performance improvements by
	 * {@link OfficeFloor}, such as {@link ThreadLocal} {@link ManagedObjectPool}
	 * solutions to reduce pool locking overheads.
	 * 
	 * @return {@link ThreadFactory} for the {@link Team}.
	 */
	ThreadFactory getThreadFactory();

}