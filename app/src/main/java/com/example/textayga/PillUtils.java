package com.example.textayga;

public class PillUtils {
    public static String getPillCountString(String count) {
        try {
            int num = Integer.parseInt(count.replaceAll("[^0-9]", ""));
            if (num % 100 >= 11 && num % 100 <= 19) {
                return num + " таблеток";
            }
            switch (num % 10) {
                case 1: return num + " таблетка";
                case 2:
                case 3:
                case 4: return num + " таблетки";
                default: return num + " таблеток";
            }
        } catch (NumberFormatException e) {
            return count + " таблеток";
        }
    }
}