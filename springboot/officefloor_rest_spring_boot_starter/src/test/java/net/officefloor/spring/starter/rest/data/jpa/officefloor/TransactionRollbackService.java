package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UncheckedRollbackException;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;

public class TransactionRollbackService {
    public void service(UserRepository userRepository) {
        userRepository.save(new User(null, "WillRollback", "test", true, null, null, null));
        throw new UncheckedRollbackException();
    }
}
