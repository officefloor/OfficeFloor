package ${package}.ria.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("counter")
public interface CounterService extends RemoteService {

	public Integer updateCount(Integer value);

}