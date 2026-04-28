package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.InterviewStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class InterviewStatusConverter implements AttributeConverter<InterviewStatus, String> {

    @Override
    public String convertToDatabaseColumn(InterviewStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public InterviewStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InterviewStatus.valueOf(dbData);
    }
}
