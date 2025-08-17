package fr.loudo.narrativecraft.util;

public class Util {

    public static String snakeCase(String value) {
        return String.join("_", value.toLowerCase().split(" "));
    }

}
