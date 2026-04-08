package net.officefloor.spring.starter.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.spring.starter.rest.data.jpa.User;
import net.officefloor.spring.starter.rest.data.jpa.UserRepository;
import net.officefloor.spring.starter.rest.web.MockComponent;
import net.officefloor.spring.starter.rest.web.MockException;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@RestController
public class MockRestController {

    private @Autowired UserRepository userRepository;

    private @Autowired PlatformTransactionManager transactionManager;


    @GetMapping("/hello/{user}")
    public ResponseEntity<String> hello(@PathVariable(name = "user") String user) {
        return ResponseEntity.ok("Hello " + user);
    }

    @GetMapping("/me")
    public String getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @GetMapping("/authentication")
    public String authentication(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/preauthorize")
    @PreAuthorize("hasRole('ACCESS')")
    public String preAuthorize() {
        return "Accessed";
    }

    @GetMapping("/secured")
    @Secured("ROLE_ACCESS")
    public String secured() {
        return "Accessed";
    }

    @GetMapping("/rolesAllowed")
    @Secured("ROLE_ACCESS")
    public String rolesAllowed() {
        return "Accessed";
    }

    @PostMapping("/requestPart")
    public String getRequestPart(@RequestPart(name = "file") MultipartFile file) throws IOException {
        String content = IOUtil.readLines(file.getInputStream()).stream().collect(Collectors.joining());
        return "file=" + file.getOriginalFilename() + ", content=" + content;
    }

    @PostMapping("/valid")
    public String valid(@Valid @RequestBody ValidRequest request) {
        return fail("Should not be invoked");
    }

    @PostMapping("/bindingResult")
    public ResponseEntity<String> bindingResult(@Valid @RequestBody ValidRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errors: " + result.getErrorCount());
        }
        return ResponseEntity.ok("OK");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidRequest {
        private @Min(1) int amount;
    }

    @GetMapping("/value")
    public String value(@Value("${officefloor.spring.test.value}") String propertyValue) {
        return propertyValue;
    }

    @GetMapping("/controllerAdvice")
    public String controllerAdvice() throws MockException {
        throw new MockException("TEST");
    }

    @GetMapping("/initBinder")
    public String initBinder(@RequestParam(name = "status") MockRestControllerAdvice.BindingTypes types) {
        switch (types) {
            case START:
                return "begin";
            case COMPLETE:
                return "end";
        }
        return null;
    }

    @GetMapping("/user/{name}")
    public String retrieveUser(@PathVariable(name = "name") String name) {
        User user = this.userRepository.findByName(name).get();
        return user.getDescription();
    }

    @GetMapping("/transaction")
    @Transactional
    public String transaction() {
        return TransactionSynchronizationManager.isActualTransactionActive() ? "Active" : "None";
    }

    @GetMapping("/noTransaction")
    public String noTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive() ? "Active" : "None";
    }

    /*
     * ========================== Data ==========================
     */

    @GetMapping("/userExists/{name}")
    public String userExists(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        return String.valueOf(this.userRepository.existsById(user.getId()));
    }

    @GetMapping("/userById/{name}")
    public String findUserById(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        return this.userRepository.findById(user.getId())
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @GetMapping("/allUsersCount")
    public int getAllUsersCount() {
        return this.userRepository.findAll().size();
    }

    @GetMapping("/userCount")
    public long getUserCount() {
        return this.userRepository.count();
    }

    @GetMapping("/users/sorted")
    public String getFirstUserSorted() {
        return this.userRepository.findAll(Sort.by("name").ascending()).get(0).getName();
    }

    @GetMapping("/activeUser/{name}")
    public String getActiveUser(@PathVariable("name") String name) {
        return this.userRepository.findActiveUserByName(name)
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @GetMapping("/userDescriptionNative/{name}")
    public String getUserDescriptionNative(@PathVariable("name") String name) {
        return this.userRepository.findDescriptionByNameNative(name);
    }

    @PostMapping("/deactivate/{name}")
    public String deactivateUser(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        int count = this.userRepository.deactivateUser(user.getId());
        return "Deactivated: " + count;
    }

    @GetMapping("/users")
    public String getPagedUsers(@RequestParam("page") int page, @RequestParam("size") int size) {
        Page<User> result = this.userRepository.findByActive(true, PageRequest.of(page, size));
        return "total=" + result.getTotalElements() + ", pages=" + result.getTotalPages()
                + ", size=" + result.getSize() + ", elements=" + result.getNumberOfElements();
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public String createUser(@RequestBody User user) {
        return this.userRepository.save(user).getName();
    }


    @DeleteMapping("/user/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        this.userRepository.deleteById(user.getId());
    }

    @PutMapping("/user/{name}")
    public String updateUser(@PathVariable("name") String name, @RequestBody UpdateRequest request) {
        User user = this.userRepository.findByName(name).orElseThrow();
        user.setDescription(request.getDescription());
        return this.userRepository.save(user).getDescription();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String description;
    }


    @GetMapping("/readOnlyTransaction")
    @Transactional(readOnly = true)
    public String readOnlyTransaction() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "ReadOnly" : "ReadWrite";
    }

    @PostMapping("/saveAndFail")
    @Transactional
    public void saveAndFail() {
        this.userRepository.save(new User(null, "WillRollback", "test", true, null, null, null));
        throw new RollbackTriggerException();
    }

    @PostMapping("/saveAndFailChecked")
    @Transactional
    public void saveAndFailChecked() throws CheckedRollbackException {
        this.userRepository.save(new User(null, "WillPersist", "checked", true, null, null, null));
        throw new CheckedRollbackException();
    }

    @PostMapping("/saveAndFailCheckedRollback")
    @Transactional(rollbackFor = Exception.class)
    public void saveAndFailCheckedRollback() throws CheckedRollbackException {
        this.userRepository.save(new User(null, "WillNotPersist", "checked", true, null, null, null));
        throw new CheckedRollbackException();
    }

    @DeleteMapping("/userByName/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteUserByName(@PathVariable("name") String name) {
        this.userRepository.deleteByName(name);
    }

    @GetMapping("/userLocked/{name}")
    @Transactional
    public String getUserWithLock(@PathVariable("name") String name) {
        return this.userRepository.findByNameWithLock(name)
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @GetMapping("/userProjection")
    public String getActiveUserProjection() {
        return this.userRepository.findSummaryByActive(true).stream()
                .map(UserRepository.UserSummary::getName)
                .sorted()
                .findFirst()
                .orElse("None");
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public int saveAllUsers(@RequestBody List<User> users) {
        return this.userRepository.saveAll(users).size();
    }


    @PostMapping("/optimisticConflict/{name}")
    public void triggerOptimisticConflict(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        Long staleVersion = user.getVersion();
        user.setDescription("First Update");
        this.userRepository.save(user);
        User staleUser = new User(user.getId(), user.getName(), "Stale Update", user.isActive(), staleVersion, null, null);
        this.userRepository.save(staleUser);
    }

    @GetMapping("/userAuditFields/{name}")
    public String getUserAuditFields(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        boolean audited = user.getCreatedAt() != null && user.getUpdatedAt() != null;
        return audited ? "Audited" : "Not Audited";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class RollbackTriggerException extends RuntimeException {
    }

    public static class CheckedRollbackException extends Exception {
    }

}
