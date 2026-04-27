package net.officefloor.spring.starter.rest.validation.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiValidRequest {
    @NotBlank
    private String name;
    @Min(1)
    private int quantity;
    @Email
    private String email;
    @Size(min = 3, max = 10)
    private String code;
}
