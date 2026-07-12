package com.example.flashsale.selfredis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CasResponse(String key, boolean swapped) {
}
