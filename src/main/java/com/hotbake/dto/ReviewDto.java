package com.hotbake.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewDto {

    @NotNull
    private Long orderItemId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank(message = "Review comment is required")
    private String comment;
}
