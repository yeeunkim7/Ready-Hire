package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.PaymentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PaymentTypeConverter implements AttributeConverter<PaymentType, String> {

    @Override
    public String convertToDatabaseColumn(PaymentType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public PaymentType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PaymentType.valueOf(dbData);
    }
}
