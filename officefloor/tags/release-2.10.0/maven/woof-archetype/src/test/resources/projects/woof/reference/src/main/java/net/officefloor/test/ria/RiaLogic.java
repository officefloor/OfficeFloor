package net.officefloor.test.ria;

import java.io.Serializable;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.test.ria.client.RiaEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Logic for <code>ria.woof.html</code>.
 */
public class RiaLogic {

	@HttpSessionStateful
	public static class Counter implements Serializable {

		private static final long serialVersionUID = 2622256490649171077L;

		private int count = 0;

		public int incrementCount(int value) {
			this.count = this.count + value;
			return this.count;
		}
	}

	/**
	 * Handles AJAX call from {@link RiaEntryPoint}.
	 * 
	 * @param value
	 *            Value passed from {@link RiaEntryPoint}. Note requires
	 *            {@link Parameter} annotation.
	 * @param callback
	 *            {@link AsyncCallback} to provide value back to
	 *            {@link RiaEntryPoint}.
	 * @param counter
	 *            {@link Counter} maintaining state of count within the HTTP
	 *            Session.
	 */
	public void updateCount(@Parameter Integer value,
			AsyncCallback<Integer> callback, Counter counter) {

		// Increment the count
		int count = counter.incrementCount(value.intValue());

		// Provide value back to GWT client
		callback.onSuccess(Integer.valueOf(count));
	}

}