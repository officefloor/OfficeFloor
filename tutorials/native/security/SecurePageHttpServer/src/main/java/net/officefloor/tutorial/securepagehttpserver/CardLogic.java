package net.officefloor.tutorial.securepagehttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpParameters;

/**
 * Logic for the card template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CardLogic {

	@Data
	@HttpParameters
	public static class CardDetails implements Serializable {
		private static final long serialVersionUID = 1L;

		private String number;

		private String month;

		private String year;

		private String csc;
	}

	public CardDetails getTemplateData(CardDetails cardDetails, ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Return the card details for rendering
		return cardDetails;
	}

	public void save(CardDetails cardDetails, ServerHttpConnection connection) {

		// Confirm a secure connection (not needed but included for tutorial)
		if (!connection.isSecure()) {
			throw new IllegalStateException();
		}

		// Logic for processing the card details
	}

}
// END SNIPPET: tutorial