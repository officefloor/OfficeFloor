package net.officefloor.tutorial.startbeforehttpserver;

import jakarta.persistence.EntityManager;
import net.officefloor.web.ObjectResponse;

/**
 * Start before logic.
 * 
 * @author Daniel Sagenschneider
 */
public class StartBeforeLogic {

	public void service(EntityManager entityManager, ObjectResponse<Message> response) {
		response.send(entityManager.find(Message.class, 1));
	}

}