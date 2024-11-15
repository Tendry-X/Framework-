package mg.itu.framework.util;

/**
 * SPRINT 6 : Conversion automatique de types
 * 
 * Convertit les String des paramètres HTTP vers les types Java appropriés.
 * 
 * @version Sprint 6 - 15 novembre 2024
 */
public class Transtipation {
    
    /**
     * Convertit une String vers le type cible
     */
    public static Object convert(String value, Class<?> targetType) throws Exception {
        if (value == null) {
            return null;
        }
        
        // String
        if (targetType == String.class) {
            return value;
        }
        
        // int / Integer
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        
        // double / Double
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        
        // float / Float
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        
        // long / Long
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        
        // boolean / Boolean
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        
        // byte / Byte
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        }
        
        // short / Short
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        }
        
        // char / Character
        if (targetType == char.class || targetType == Character.class) {
            return value.charAt(0);
        }
        
        throw new Exception("Type non supporté : " + targetType.getName());
    }
}