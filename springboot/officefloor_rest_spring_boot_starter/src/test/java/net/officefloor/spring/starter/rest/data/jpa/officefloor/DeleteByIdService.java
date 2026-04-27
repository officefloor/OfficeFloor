package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import org.springframework.web.bind.annotation.PathVariable;

public class DeleteByIdService {
    public void service(@PathVariable(name = "name") String name,
                        UserRepository userRepository) {
        User user = userRepository.findByName(name).orElseThrow();
        userRepository.deleteById(user.getId());
    }
}
