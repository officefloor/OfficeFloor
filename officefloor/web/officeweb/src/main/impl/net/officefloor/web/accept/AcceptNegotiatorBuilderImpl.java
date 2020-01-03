package net.officefloor.web.accept;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.accept.AcceptNegotiatorImpl.AcceptHandler;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * {@link AcceptNegotiatorBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AcceptNegotiatorBuilderImpl<H> implements AcceptNegotiatorBuilder<H> {

	/**
	 * {@link AcceptHandler} instances.
	 */
	private final List<AcceptHandler<H>> acceptHandlers = new LinkedList<>();

	/*
	 * =================== AcceptNegotiatorBuilder =============================
	 */

	@Override
	public void addHandler(String contentType, H handler) {
		this.acceptHandlers.add(AcceptNegotiatorImpl.createAcceptHandler(contentType, handler));
	}

	@Override
	@SuppressWarnings("unchecked")
	public AcceptNegotiator<H> build() throws NoAcceptHandlersException {

		// Ensure have at least one accept handler
		if (this.acceptHandlers.size() == 0) {
			throw new NoAcceptHandlersException("Must have at least one " + AcceptHandler.class.getSimpleName()
					+ " configured for the " + AcceptNegotiator.class.getSimpleName());
		}

		// Return the negotiator
		return new AcceptNegotiatorImpl<>(this.acceptHandlers.toArray(new AcceptHandler[this.acceptHandlers.size()]));
	}

}