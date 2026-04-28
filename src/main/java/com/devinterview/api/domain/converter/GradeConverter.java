package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.Grade;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GradeConverter implements AttributeConverter<Grade, String> {

    @Override
    public String convertToDatabaseColumn(Grade attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Grade convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Grade.valueOf(dbData);
    }
}
