package com.nexus.github.dto;

public record ConnectGitHubRequest(
        String code,
        String accessToken,
        String username,
        String scopes
) {}
