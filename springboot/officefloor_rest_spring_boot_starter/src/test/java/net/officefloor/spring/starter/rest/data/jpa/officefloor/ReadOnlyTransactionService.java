package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReadOnlyTransactionService {
    public void service(UserRepository userRepository,
                        ObjectResponse<String> response) {
        response.send(TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "ReadOnly" : "ReadWrite");
    }
}
