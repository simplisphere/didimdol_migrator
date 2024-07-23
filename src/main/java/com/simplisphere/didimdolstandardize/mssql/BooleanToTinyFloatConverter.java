package com.simplisphere.didimdolstandardize.mssql;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToTinyFloatConverter implements AttributeConverter<Boolean, Float> {

    @Override
    public Float convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? 1F : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Float dbData) {
        return dbData != null && dbData == 1F;
    }
}