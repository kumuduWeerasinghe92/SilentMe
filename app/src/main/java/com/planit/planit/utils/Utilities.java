package com.planit.planit.utils;



public class Utilities {

    public static String encodeKey(String email)
    {
        return email.replace('.', ',');
    }

    public static String decodeKey(String email)
    {
        return email.replace(',', '.');
    }

}
