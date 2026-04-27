package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Sort;

public class SortedService {
    public void service(UserRepository userRepository,
                        ObjectResponse<String> response) {
        response.send(userRepository.findAll(Sort.by("name").ascending()).get(0).getName());
    }
}
