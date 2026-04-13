package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

public class NativeSqlQueryService {
    public void service(@PathVariable(name = "name") String name,
                        UserRepository userRepository,
                        ObjectResponse<String> response) {
        response.send(userRepository.findDescriptionByNameNative(name));
    }
}
