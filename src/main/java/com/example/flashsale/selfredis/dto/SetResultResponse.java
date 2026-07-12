package com.example.flashsale.selfredis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SetResultResponse(String key, boolean stored) {
}
