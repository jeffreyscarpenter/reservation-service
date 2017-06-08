package com.cassandraguide.services.reservation;

import org.apache.commons.lang3.RandomStringUtils;

public class ConfirmationNumberGenerator {


    private static final int RANDOM_STRING_COUNT = 6;
    private static char[] charSet;

    public static String getConfirmationNumber()
    {
        if (charSet == null) {
            charSet = new char[36]; // 0-9 and A-Z
            int j = 0;
            for (int i = 48; i <= 57; ++i) {
                charSet[j++] = (char)i;
            }
            for (int i = 65; i <= 90; ++i) {
                charSet[j++] = (char)i;
            }
        }

        return RandomStringUtils.random(RANDOM_STRING_COUNT, charSet);
    }
    
}
