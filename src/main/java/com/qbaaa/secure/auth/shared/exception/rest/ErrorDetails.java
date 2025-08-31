package com.qbaaa.secure.auth.shared.exception.rest;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorDetails(
    LocalDateTime localDateTime, UUID uuid, String code, String description) {}
