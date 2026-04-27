package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import org.springframework.web.bind.annotation.PathVariable;

public class OptimisticConflictService {
    public void service(@PathVariable(name = "name") String name,
                        UserRepository userRepository) {
        User user = userRepository.findByName(name).orElseThrow();
        Long staleVersion = user.getVersion();
        user.setDescription("First Update");
        userRepository.save(user);
        User staleUser = new User(user.getId(), user.getName(), "Stale Update", user.isActive(), staleVersion, null, null);
        userRepository.save(staleUser);
    }
}
