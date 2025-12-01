package mg.itu.framework.util;

public class Transtipation {
    public static Object convert(String value, Class<?> type) {
        if (type.equals(int.class) || type.equals(Integer.class))
            return Integer.parseInt(value);
        if (type.equals(double.class) || type.equals(Double.class))
            return Double.parseDouble(value);
        if (type.equals(long.class) || type.equals(Long.class))
            return Long.parseLong(value);
        if (type.equals(float.class) || type.equals(Float.class))
            return Float.parseFloat(value);
        if (type.equals(boolean.class) || type.equals(Boolean.class))
            return Boolean.parseBoolean(value);
        return value;
    }
}
