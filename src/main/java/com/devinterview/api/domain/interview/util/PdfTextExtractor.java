package com.devinterview.api.domain.interview.util;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class PdfTextExtractor {

    private static final int MAX_TEXT_LENGTH = 5000;
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    public String extract(MultipartFile file) {
        validateFile(file);
        log.info("[PDF] Parsing started: name={}, size={}", file.getOriginalFilename(), file.getSize());

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(document);
            String text = normalizeAndTruncate(rawText);

            if (text.isBlank()) {
                throw new CustomException(ErrorCode.PDF_PARSE_ERROR, "PDF에서 텍스트를 추출할 수 없습니다.");
            }

            log.info("[PDF] Parsing completed: charCount={}", text.length());
            return text;
        } catch (CustomException ex) {
            throw ex;
        } catch (IOException ex) {
            log.warn("[PDF] Parsing failed: {}", ex.getMessage());
            throw new CustomException(ErrorCode.PDF_PARSE_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.PDF_INVALID_FORMAT);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CustomException(ErrorCode.PDF_TOO_LARGE);
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isPdfContentType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfExtension = filename != null && filename.toLowerCase().endsWith(".pdf");
        if (!isPdfContentType && !isPdfExtension) {
            throw new CustomException(ErrorCode.PDF_INVALID_FORMAT);
        }
    }

    private String normalizeAndTruncate(String rawText) {
        if (rawText == null) {
            return "";
        }
        String normalized = rawText.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_TEXT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_TEXT_LENGTH);
    }
}
