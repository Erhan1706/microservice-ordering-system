package nl.tudelft.sem.template.authentication.domain.user.valueobjects.converters;

import nl.tudelft.sem.template.authentication.domain.user.valueobjects.UserRole;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA Converter for the UserRole value object.
 */
@Converter
public class UserRoleAttributeConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute.toString();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return new UserRole(dbData);
    }
}
