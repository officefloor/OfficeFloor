package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.server.http.request.HttpRequestTest;

/**
 * Configuration of a {@link HttpRequestTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunConfig {

	/**
	 * {@link CommunicationConfig} instances.
	 */
	public final List<CommunicationConfig> communications = new LinkedList<CommunicationConfig>();

	public void addCommunication(CommunicationConfig communication) {
		this.communications.add(communication);
	}
}
