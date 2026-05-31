package net.officefloor.spring.starter.rest.officefloor.officefloor;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

public class TeamService {

    public static interface SocketFlow {
        void team(String threadName);
    }

    public void socketThread(@Flow("team") SocketFlow flow) {
        flow.team(Thread.currentThread().getName());
    }

    public void teamThread(@Parameter String socketThreadName, TeamDependency dependency, ObjectResponse<String> response) {
        String teamThread = Thread.currentThread().getName();
        response.send(socketThreadName + "---" + teamThread);
    }

}
