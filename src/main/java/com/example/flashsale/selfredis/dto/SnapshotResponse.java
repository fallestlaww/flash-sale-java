package com.example.flashsale.selfredis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SnapshotResponse(String path, int size) {
}
