package net.officefloor.spring.starter.rest.data.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spring/data/jpa")
public class DataJpaRestController {

    private @Autowired UserRepository userRepository;

    private @Autowired PlatformTransactionManager transactionManager;

    @GetMapping("/existsById/{name}")
    public String existsById(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        return String.valueOf(this.userRepository.existsById(user.getId()));
    }

    @GetMapping("/findById/{name}")
    public String findById(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        return this.userRepository.findById(user.getId())
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @GetMapping("/findByName/{name}")
    public String findByName(@PathVariable(name = "name") String name) {
        User user = this.userRepository.findByName(name).get();
        return user.getDescription();
    }

    @GetMapping("/findAll/size")
    public int findAll() {
        return this.userRepository.findAll().size();
    }

    @GetMapping("/count")
    public long count() {
        return this.userRepository.count();
    }

    @GetMapping("/sorted")
    public String sorted() {
        return this.userRepository.findAll(Sort.by("name").ascending()).get(0).getName();
    }

    @GetMapping("/customQuery/{name}")
    public String customQuery(@PathVariable("name") String name) {
        return this.userRepository.findActiveUserByName(name)
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @GetMapping("/nativeSqlQuery/{name}")
    public String nativeQuery(@PathVariable("name") String name) {
        return this.userRepository.findDescriptionByNameNative(name);
    }

    @PostMapping("/modifyingQuery/{name}")
    public String modifyingQuery(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        int count = this.userRepository.deactivateUser(user.getId());
        return "Deactivated: " + count;
    }

    @GetMapping("/pagination")
    public String pagination(@RequestParam("page") int page, @RequestParam("size") int size) {
        Page<User> result = this.userRepository.findByActive(true, PageRequest.of(page, size));
        return "total=" + result.getTotalElements() + ", pages=" + result.getTotalPages()
                + ", size=" + result.getSize() + ", elements=" + result.getNumberOfElements();
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public String save(@RequestBody User user) {
        return this.userRepository.save(user).getName();
    }

    @PostMapping("/saveAll")
    @ResponseStatus(HttpStatus.CREATED)
    public int saveAll(@RequestBody List<User> users) {
        return this.userRepository.saveAll(users).size();
    }

    @DeleteMapping("/deleteById/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        this.userRepository.deleteById(user.getId());
    }

    @DeleteMapping("/deleteByName/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteByName(@PathVariable("name") String name) {
        this.userRepository.deleteByName(name);
    }

    @PutMapping("/update/{name}")
    public String update(@PathVariable("name") String name, @RequestBody UpdateRequest request) {
        User user = this.userRepository.findByName(name).orElseThrow();
        user.setDescription(request.getDescription());
        return this.userRepository.save(user).getDescription();
    }

    @GetMapping("/description/{name}")
    public String description(@PathVariable("name") String name) {
        return this.userRepository.findByName(name).orElseThrow().getDescription();
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

    @GetMapping("/readOnlyTransaction")
    @Transactional(readOnly = true)
    public String readOnlyTransaction() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "ReadOnly" : "ReadWrite";
    }

    @PostMapping("/saveAndFail")
    @Transactional
    public void saveAndFail() {
        this.userRepository.save(new User(null, "WillRollback", "test", true, null, null, null));
        throw new UncheckedRollbackException();
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

    @GetMapping("/pessimisticLock/{name}")
    @Transactional
    public String pessimisticLock(@PathVariable("name") String name) {
        return this.userRepository.findByNameWithLock(name)
                .map(User::getDescription)
                .orElse("Not Found");
    }

    @PostMapping("/optimisticConflict/{name}")
    public void optimisticConflict(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        Long staleVersion = user.getVersion();
        user.setDescription("First Update");
        this.userRepository.save(user);
        User staleUser = new User(user.getId(), user.getName(), "Stale Update", user.isActive(), staleVersion, null, null);
        this.userRepository.save(staleUser);
    }

    @GetMapping("/projection")
    public String projection() {
        return this.userRepository.findSummaryByActive(true).stream()
                .map(UserRepository.UserSummary::getName)
                .sorted()
                .findFirst()
                .orElse("None");
    }

    @GetMapping("/auditFields/{name}")
    public String auditFields(@PathVariable("name") String name) {
        User user = this.userRepository.findByName(name).orElseThrow();
        boolean audited = user.getCreatedAt() != null && user.getUpdatedAt() != null;
        return audited ? "Audited" : "Not Audited";
    }

}
