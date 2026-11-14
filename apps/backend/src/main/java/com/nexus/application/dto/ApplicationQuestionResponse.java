package com.nexus.application.dto;

import java.util.List;

public record ApplicationQuestionResponse(
        String question,
        String suggestedAnswer,
        List<String> keyTalkingPoints,
        String advisoryNotice
) {}
