package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;

public class FindAllSizeService {
    public void service(UserRepository userRepository,
                        ObjectResponse<Integer> response) {
        response.send(userRepository.findAll().size());
    }
}
