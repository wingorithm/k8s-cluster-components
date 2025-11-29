package com.kubeservice.a_service.dto;

public record BookResponse(
        Long id,
        String title,
        String author
) {}
