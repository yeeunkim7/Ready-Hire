package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.PlanType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PlanTypeConverter implements AttributeConverter<PlanType, String> {

    @Override
    public String convertToDatabaseColumn(PlanType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public PlanType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PlanType.valueOf(dbData);
    }
}
