package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.SubscriptionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SubscriptionStatusConverter implements AttributeConverter<SubscriptionStatus, String> {

    @Override
    public String convertToDatabaseColumn(SubscriptionStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public SubscriptionStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SubscriptionStatus.valueOf(dbData);
    }
}
