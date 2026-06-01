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

package net.officefloor.spring.starter.rest.data.jpa.common;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Page<User> findByActive(boolean active, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.name = :name and u.active = true")
    Optional<User> findActiveUserByName(@Param("name") String name);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = false WHERE u.id = :id")
    int deactivateUser(@Param("id") Long id);

    @Query(value = "SELECT description FROM users WHERE name = :name", nativeQuery = true)
    String findDescriptionByNameNative(@Param("name") String name);

    @Transactional
    void deleteByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.name = :name")
    Optional<User> findByNameWithLock(@Param("name") String name);

    interface UserSummary {
        String getName();
    }

    List<UserSummary> findSummaryByActive(boolean active);

}
