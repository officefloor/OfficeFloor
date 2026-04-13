package net.officefloor.spring.starter.rest.data.jpa;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.data.jpa.common.UpdateRequest;
import net.officefloor.spring.starter.rest.data.jpa.common.User;
import net.officefloor.spring.starter.rest.data.jpa.common.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractDataJpaVerification extends AbstractMockMvcVerification {

    private @Autowired UserRepository userRepository;

    @BeforeEach
    public void loadTestData() {
        for (int i = 1; i < 100; i++) {
            this.userRepository.save(new User(null, "User_" + i, "Description_" + i, true, null, null, null));
        }
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll();
    }

    @Test
    public void existsById() throws Exception {
        this.mvc.perform(get(this.getPath("/existsById/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("true")));
    }

    @Test
    public void findById() throws Exception {
        this.mvc.perform(get(this.getPath("/findById/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    public void findByName() throws Exception {
        this.mvc.perform(get(this.getPath("/findByName/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    public void findAllSize() throws Exception {
        this.mvc.perform(get(this.getPath("/findAll/size")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

    @Test
    public void count() throws Exception {
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

    @Test
    public void sorted() throws Exception {
        this.mvc.perform(get(this.getPath("/sorted")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User_1")));
    }

    @Test
    public void customQuery() throws Exception {
        this.mvc.perform(get(this.getPath("/customQuery/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    public void nativeSqlQuery() throws Exception {
        this.mvc.perform(get(this.getPath("/nativeSqlQuery/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    public void modifyingQuery() throws Exception {
        this.mvc.perform(post(this.getPath("/modifyingQuery/User_1")).with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Deactivated: 1")));
        this.mvc.perform(get(this.getPath("/customQuery/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Not Found")));
    }

    @Test
    public void pagination() throws Exception {
        this.mvc.perform(get(this.getPath("/pagination?page=0&size=10")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("total=99, pages=10, size=10, elements=10")));
    }

    @Test
    public void paginationNextPage() throws Exception {
        this.mvc.perform(get(this.getPath("/pagination?page=1&size=10")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("total=99, pages=10, size=10, elements=10")));
    }

    @Test
    public void paginationLastPage() throws Exception {
        this.mvc.perform(get(this.getPath("/pagination?page=9&size=10")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("total=99, pages=10, size=10, elements=9")));
    }

    @Test
    public void save() throws Exception {
        this.mvc.perform(post(this.getPath("/save")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User(null, "NewUser", "New Description", true, null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("NewUser")));
    }

    @Test
    public void duplicateSave() throws Exception {
        this.mvc.perform(post(this.getPath("/save")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User(null, "User_1", "Duplicate", true, null, null, null))))
                .andExpect(status().isConflict());
    }

    @Test
    public void saveAll() throws Exception {
        this.mvc.perform(post(this.getPath("/saveAll")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User[] {
                                new User(null, "Batch1", "batch", true, null, null, null),
                                new User(null, "Batch2", "batch", true, null, null, null)
                        })))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("2")));
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("101")));
    }

    @Test
    public void deleteById() throws Exception {
        this.mvc.perform(delete(this.getPath("/deleteById/User_1")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        this.mvc.perform(get(this.getPath("/customQuery/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Not Found")));
    }

    @Test
    public void deleteByName() throws Exception {
        this.mvc.perform(delete(this.getPath("/deleteByName/User_1")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("98")));
    }

    @Test
    public void update() throws Exception {
        this.mvc.perform(put(this.getPath("/update/User_1")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateRequest("Updated Description"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Updated Description")));
        this.mvc.perform(get(this.getPath("/description/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Updated Description")));
    }

    @Test
    public void transaction() throws Exception {
        this.mvc.perform(get(this.getPath("/transaction")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Active")));
    }

    @Test
    public void noTransaction() throws Exception {
        this.mvc.perform(get(this.getPath("/noTransaction")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("None")));
    }

    @Test
    public void readOnlyTransaction() throws Exception {
        this.mvc.perform(get(this.getPath("/readOnlyTransaction")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ReadOnly")));
    }

    @Test
    public void transactionRollback() throws Exception {
        this.mvc.perform(post(this.getPath("/transactionRollback")).with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

    @Test
    public void checkedExceptionNoRollback() throws Exception {
        this.mvc.perform(post(this.getPath("/checkedExceptionNoRollback")).with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("100")));
    }

    @Test
    public void checkedExceptionWithRollback() throws Exception {
        this.mvc.perform(post(this.getPath("/checkedExceptionWithRollback")).with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        this.mvc.perform(get(this.getPath("/count")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

    @Test
    public void pessimisticLock() throws Exception {
        this.mvc.perform(get(this.getPath("/pessimisticLock/User_1")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    public void optimisticConflict() throws Exception {
        this.mvc.perform(post(this.getPath("/optimisticConflict/User_1")).with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void projection() throws Exception {
        this.mvc.perform(get(this.getPath("/projection")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User_1")));
    }

    @Test
    public void auditingFields() throws Exception {
        this.mvc.perform(post(this.getPath("/save")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User(null, "AuditedUser", "Audit Test", true, null, null, null))))
                .andExpect(status().isCreated());
        this.mvc.perform(get(this.getPath("/auditFields/AuditedUser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Audited")));
    }

}
