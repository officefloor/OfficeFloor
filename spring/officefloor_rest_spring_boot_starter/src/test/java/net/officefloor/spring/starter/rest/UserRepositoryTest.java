package net.officefloor.spring.starter.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

    private @Autowired UserRepository userRepository;

    @BeforeEach
    public void loadTestData() {
        for (int i = 1; i < 100; i++) {
            this.userRepository.save(new User(null, "User_" + i, true));
        }
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll();
    }

    @Test
    public void getUser() {
        final String USER_1 = "User_1";
        User user = this.userRepository.findByName(USER_1).get();
        assertNotNull(user, "Should find user");
        assertEquals(USER_1, user.getName(), "Incorrect user");
        assertTrue(user.isActive(), "User should be active");
    }
}
