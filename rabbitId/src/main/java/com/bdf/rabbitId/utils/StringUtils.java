package com.bdf.rabbitId.utils;


public class StringUtils {

    /**
     * Checks if a CharSequence is not empty (""), not null and not whitespace only.
     *
     * @param cs the CharSequence to check, may be null
     * @return
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Checks if a CharSequence is empty (""), null or whitespace only.
     *
     * @param cs the CharSequence to check, may be null
     * @return
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a CharSequence is empty (""), null.
     *
     * @param cs the CharSequence to check, may be null
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks if a CharSequence is not empty ("") and not null.
     *
     * @param cs the CharSequence to check, may be null
     * @return
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }




}
