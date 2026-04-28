package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.CareerLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CareerLevelConverter implements AttributeConverter<CareerLevel, String> {

    @Override
    public String convertToDatabaseColumn(CareerLevel attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CareerLevel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CareerLevel.valueOf(dbData);
    }
}
