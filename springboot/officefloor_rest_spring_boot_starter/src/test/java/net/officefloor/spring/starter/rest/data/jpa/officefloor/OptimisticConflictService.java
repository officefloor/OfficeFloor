/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
