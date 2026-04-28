package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.AuthProvider;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AuthProviderConverter implements AttributeConverter<AuthProvider, String> {

    @Override
    public String convertToDatabaseColumn(AuthProvider attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AuthProvider convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AuthProvider.valueOf(dbData);
    }
}
