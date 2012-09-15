package net.officefloor.test.ria.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CounterServiceAsync {

	public void updateCount(Integer value, AsyncCallback<Integer> callback);

}