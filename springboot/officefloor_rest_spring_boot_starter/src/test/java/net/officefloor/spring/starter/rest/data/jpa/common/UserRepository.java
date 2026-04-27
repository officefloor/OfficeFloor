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
