package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.SubscriptionPlanType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SubscriptionPlanTypeConverter implements AttributeConverter<SubscriptionPlanType, String> {

    @Override
    public String convertToDatabaseColumn(SubscriptionPlanType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public SubscriptionPlanType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SubscriptionPlanType.valueOf(dbData);
    }
}
