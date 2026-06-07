package com.devinterview.api.domain.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewHistoryPageResponse {

    private final List<InterviewHistoryResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
