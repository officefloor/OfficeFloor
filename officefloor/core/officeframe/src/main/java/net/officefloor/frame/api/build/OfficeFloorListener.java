package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Listens to the open/close of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorListener {

	/**
	 * Notifies that the {@link OfficeFloor} has been opened.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle open listen logic.
	 */
	void officeFloorOpened(OfficeFloorEvent event) throws Exception;

	/**
	 * Notifies that the {@link OfficeFloor} has been closed.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle close listen logic.
	 */
	void officeFloorClosed(OfficeFloorEvent event) throws Exception;

}