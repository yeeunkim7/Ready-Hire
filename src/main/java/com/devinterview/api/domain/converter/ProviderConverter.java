package com.devinterview.api.domain.converter;

import com.devinterview.api.domain.enums.Provider;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProviderConverter implements AttributeConverter<Provider, String> {

    @Override
    public String convertToDatabaseColumn(Provider attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Provider convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Provider.valueOf(dbData);
    }
}
