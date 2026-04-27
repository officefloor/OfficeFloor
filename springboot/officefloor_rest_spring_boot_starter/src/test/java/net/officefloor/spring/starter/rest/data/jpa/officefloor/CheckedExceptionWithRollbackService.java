package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.CheckedRollbackException;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;

public class CheckedExceptionWithRollbackService {
    public void service(UserRepository userRepository) throws CheckedRollbackException {
        userRepository.save(new User(null, "WillNotPersist", "checked", true, null, null, null));
        throw new CheckedRollbackException();
    }
}
