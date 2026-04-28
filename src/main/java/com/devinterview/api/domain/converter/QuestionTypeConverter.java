package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.QuestionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class QuestionTypeConverter implements AttributeConverter<QuestionType, String> {

    @Override
    public String convertToDatabaseColumn(QuestionType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public QuestionType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : QuestionType.valueOf(dbData);
    }
}
