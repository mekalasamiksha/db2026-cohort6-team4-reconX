package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusUpdate(

    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "PENDING|MATCHED|UNMATCHED|DISPUTED|CANCELLED",
        message = "Invalid status. Allowed values: PENDING, MATCHED, UNMATCHED, DISPUTED, CANCELLED"
    )
    String status

) {}