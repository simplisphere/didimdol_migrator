package com.simplisphere.didimdolstandardize.firebird;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OriginalSpeciesConverter implements AttributeConverter<OriginSpecies, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OriginSpecies originSpecies) {
        if (originSpecies == null) {
            return null;
        }
        return originSpecies.getId();
    }

    @Override
    public OriginSpecies convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return OriginSpecies.of(id);
    }
}