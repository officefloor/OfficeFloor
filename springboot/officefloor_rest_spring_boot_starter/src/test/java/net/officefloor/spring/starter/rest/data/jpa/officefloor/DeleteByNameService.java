package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import org.springframework.web.bind.annotation.PathVariable;

public class DeleteByNameService {
    public void service(@PathVariable(name = "name") String name,
                        UserRepository userRepository) {
        userRepository.deleteByName(name);
    }
}
