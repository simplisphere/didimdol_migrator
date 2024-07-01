package com.simplisphere.didimdolstandardize.firebird;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SosulOriginalSpeciesConverter implements AttributeConverter<SosulOriginSpecies, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SosulOriginSpecies sosulOriginSpecies) {
        if (sosulOriginSpecies == null) {
            return null;
        }
        return sosulOriginSpecies.getId();
    }

    @Override
    public SosulOriginSpecies convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return SosulOriginSpecies.of(id);
    }
}