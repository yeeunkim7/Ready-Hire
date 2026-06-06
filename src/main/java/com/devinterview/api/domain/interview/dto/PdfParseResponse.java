package com.devinterview.api.domain.interview.dto;

/**
 * PDF 텍스트 추출 결과 DTO.
 */
public record PdfParseResponse(
    String text,
    int charCount
) {
}
