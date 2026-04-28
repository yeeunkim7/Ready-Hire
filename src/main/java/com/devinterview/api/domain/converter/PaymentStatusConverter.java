package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PaymentStatus.valueOf(dbData);
    }
}
