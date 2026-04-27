package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;

public class CountService {
    public void service(UserRepository userRepository,
                        ObjectResponse<Long> response) {
        response.send(userRepository.count());
    }
}
