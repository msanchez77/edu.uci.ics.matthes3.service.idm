package edu.uci.ics.matthes3.service.idm.core;

import java.util.Arrays;

public class ValidatePassword {
    public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 16;
    // CASE -12
    public static boolean valEmptyOrNull(char[] password) {
        boolean bool = (password != null && password.length != 0);
        password = null;
        return bool;
    }

    // CASE 12
    public static boolean valLength(char[] password) {
        boolean bool = password.length >= MIN_PASSWORD_LENGTH && password.length <= MAX_PASSWORD_LENGTH;
        password = null;
        return bool;
    }

    // CASE 13
    public static boolean valFormat(char[] password) {
        // At least one upper case alpha
        boolean upperCase = false;
        // At least one lower case alpha
        boolean lowerCase = false;
        // At least one numeric
        boolean numeric = false;
        // At least one special character
        boolean specialChar = false;

        for (char c: password) {
            if (Character.isLowerCase(c)) {
                lowerCase = true;
                continue;
            }
            if (Character.isUpperCase(c)) {
                upperCase = true;
                continue;
            }
            if (Character.isDigit(c)) {
                numeric = true;
                continue;
            }
            specialChar = isSpecial(c);

        }

        return upperCase && lowerCase && numeric && specialChar;
    }

    private static boolean isSpecial(char c) {
        String specials = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        for (int i = 0; i < specials.length(); i++) {
            if (c == specials.charAt(i)) {
                return true;
            }
        }
        return false;
    }
}
