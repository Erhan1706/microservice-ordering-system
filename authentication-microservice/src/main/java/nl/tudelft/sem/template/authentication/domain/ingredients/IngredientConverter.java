package nl.tudelft.sem.template.authentication.domain.ingredients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter for Ingredient value object.
 */
@Converter
public class IngredientConverter implements AttributeConverter<List<Long>, String> {


    @Override
    public String convertToDatabaseColumn(List<Long> allergies) {
        StringBuilder sb = new StringBuilder();
        for (Long l : allergies) {
            sb.append(l);
            sb.append(",");
        }

        return sb.toString();
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        //String[] ig = dbData.split(" ");
        //List<Long> res = new ArrayList<>();
        // res.add(0L);
        //for (String l : ig) {
        //  res.add(NumberUtils.parseNumber(l, Long.class));
        //}

        return dbData == null ? Collections.emptyList() :
                Arrays.stream(dbData.split(",")).map(Long::parseLong).collect(Collectors.toList())
                ;
    }
}