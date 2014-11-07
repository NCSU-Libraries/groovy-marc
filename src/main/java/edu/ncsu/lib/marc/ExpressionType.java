package edu.ncsu.lib.marc;

/**
 * Created by ajconsta on 7/17/14.
 */
public enum ExpressionType {
    CONTROL,
    // repeatable field
    REPEATABLE_FIELD,
    NONREPEATABLE_FIELD,
    // repeatable subfield of repeatable OR non-repeatable subfield
    REPEATABLE_SUBFIELD,
    // non-repeatable subfield of non repeatable field
    NONREPEATABLE_SUBFIELD

}
