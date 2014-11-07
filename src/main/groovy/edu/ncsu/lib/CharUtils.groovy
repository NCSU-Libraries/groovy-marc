package edu.ncsu.lib

import groovy.transform.Memoized

/**
 * Implements static methods for generating char objects from strings, which Groovy does not have good
 * native support for.
 */
class CharUtils {

    @Memoized
    public static char toChar(String str) {
        return str.charAt(0)
    }
}
