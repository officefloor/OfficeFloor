package net.officefloor.example.weborchestration.invoice;

import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.Quote;

/**
 * Input for {@link Invoice}.
 * 
 * @author daniel
 */
public class InvoiceInput {

	/**
	 * {@link Quote} Id.
	 */
	private String quoteId;

	/**
	 * Specifies the {@link Quote} Id.
	 * 
	 * @param quoteId
	 *            {@link Quote} Id.
	 */
	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	/**
	 * Obtains the {@link Quote} Id.
	 * 
	 * @return {@link Quote} Id.
	 */
	public String getQuoteId() {
		return this.quoteId;
	}

}