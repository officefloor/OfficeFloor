package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * Augments the {@link Team} instances within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamAugmentor {

	/**
	 * Augments the {@link Team}.
	 * 
	 * @param context {@link TeamAugmentorContext}.
	 */
	void augmentTeam(TeamAugmentorContext context);

}