package net.officefloor.spring.starter.rest.validation.common;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidRequest {
    private @Min(1) int amount;
}
