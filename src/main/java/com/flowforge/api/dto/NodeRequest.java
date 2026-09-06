package com.flowforge.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record NodeRequest(
        @NotBlank String id,
        @NotBlank String type,
        Map<String, Object> config
) {
}
