package com.cs420;

public class ThemeManager {

    private static boolean cuteTheme = true;

    public static void toggleTheme() {
        cuteTheme = !cuteTheme;
    }

    public static String getThemeFile() {
        if (cuteTheme) {
            return "cute.css";
        } else {
            return "cool.css";
        }
    }

    public static boolean isCuteTheme() {
        return cuteTheme;
    }
}