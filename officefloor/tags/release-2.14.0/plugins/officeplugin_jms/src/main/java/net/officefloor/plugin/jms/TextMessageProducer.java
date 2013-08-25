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
package net.officefloor.plugin.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Publishes {@link javax.jms.TextMessage} instances to a
 * {@link javax.jms.Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TextMessageProducer {

	/**
	 * Sends text within payload of a {@link javax.jms.Message}.
	 * 
	 * @param text
	 *            Text for the payload of the {@link javax.jms.Message}.
	 */
	void send(String text) throws JMSException;

}

/**
 * Implementation of the {@link TextMessageProducer}.
 */
class TextMessageProducerImpl implements TextMessageProducer {

	/**
	 * {@link Session}.
	 */
	protected final Session session;

	/**
	 * {@link Destination}.
	 */
	private final Destination destination;

	/**
	 * Lazy created producer to enqueue forms.
	 */
	protected MessageProducer producer = null;

	/**
	 * Initiate.
	 * 
	 * @param session
	 *            {@link Session}.
	 * @param destination
	 *            {@link Destination}.
	 */
	public TextMessageProducerImpl(Session session, Destination destination) {
		this.session = session;
		this.destination = destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.TextMessageProducer#send(java.lang.String)
	 */
	public void send(String text) throws JMSException {
		// Lazy load if required
		if (this.producer == null) {
			this.producer = this.session.createProducer(this.destination);
		}

		// Enqueue the text
		TextMessage message = this.session.createTextMessage(text);
		this.producer.send(message);
	}

}