package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.spring.starter.rest.data.jpa.common.CheckedRollbackException;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;

public class CheckedExceptionNoRollbackService {

    public void service(
            UserRepository userRepository) throws CheckedRollbackException {
        userRepository.save(new User(null, "WillPersist", "checked", true, null, null, null));
        throw new CheckedRollbackException();
    }

    public void handle(
            @Parameter CheckedRollbackException ex,
            @HttpResponse(status = 500) ObjectResponse<String> response) {
        response.send("");
    }
}
