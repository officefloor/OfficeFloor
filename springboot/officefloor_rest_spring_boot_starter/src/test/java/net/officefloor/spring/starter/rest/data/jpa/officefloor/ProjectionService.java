package net.officefloor.spring.starter.rest.data.jpa.officefloor;

import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import net.officefloor.web.ObjectResponse;

public class ProjectionService {
    public void service(UserRepository userRepository,
                        ObjectResponse<String> response) {
        response.send(userRepository.findSummaryByActive(true).stream()
                .map(UserRepository.UserSummary::getName)
                .sorted()
                .findFirst()
                .orElse("None"));
    }
}
