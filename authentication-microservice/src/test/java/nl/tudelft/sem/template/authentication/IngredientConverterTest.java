package nl.tudelft.sem.template.authentication;

import nl.tudelft.sem.template.authentication.domain.ingredients.IngredientConverter;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class IngredientConverterTest {

    private IngredientConverter ingredientConverter;

    @BeforeEach
    public void setup() {
        ingredientConverter = new IngredientConverter();
    }

    @Test
    public void testConvertToDatabaseColumnEmpty() {
        List<Long> allergies = new ArrayList<>();

        String result = ingredientConverter.convertToDatabaseColumn(allergies);
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testConvertToDatabaseColumn() {
        List<Long> allergies = new ArrayList<>();
        allergies.add(1L);
        allergies.add(2L);

        String result = ingredientConverter.convertToDatabaseColumn(allergies);
        assertThat(result).isEqualTo("1,2,");
    }

    @Test
    public void testConvertToEntityAttribute() {
        List<Long> allergies = new ArrayList<>();
        allergies.add(1L);
        allergies.add(2L);

        String result = ingredientConverter.convertToDatabaseColumn(allergies);
        assertThat(allergies).isEqualTo(ingredientConverter.convertToEntityAttribute(result));
    }

    @Test
    public void testConvertToEntityAttributeEmpty() {
        ArrayList emptyList = new ArrayList();
        assertThat(emptyList).isEqualTo(ingredientConverter.convertToEntityAttribute(null));
    }
}
