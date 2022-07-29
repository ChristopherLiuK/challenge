package com.db.awmd.challenge.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class TransferDTO {

    @NotEmpty
    private String accountFromId;

    @NotEmpty
    private String accountToId;

    @NotNull
    private BigDecimal amount;

}