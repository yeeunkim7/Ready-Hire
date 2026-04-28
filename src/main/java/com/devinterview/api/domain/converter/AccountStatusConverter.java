package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.AccountStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccountStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AccountStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccountStatus.valueOf(dbData);
    }
}
