package dev.dolu.userservice.utils;

public class EnumUtils {
    public static <E extends Enum<E>> E safeValueOf(Class<E> enumClass, String value) {
        if (value == null) return null;
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value.trim())) {
                return constant;
            }
        }
        return null;
    }
}

