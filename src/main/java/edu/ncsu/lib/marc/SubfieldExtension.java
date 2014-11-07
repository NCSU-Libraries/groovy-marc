package edu.ncsu.lib.marc;

import org.marc4j.marc.Subfield;

/**
 * Created by ajconsta on 7/22/14.
 */
public class SubfieldExtension {


    public static String leftShift(Subfield subfield, String value) {
        subfield.setData( subfield.getData() + value );
        return subfield.getData();
    }

    public static Subfield fromString( Subfield subfield, String subfieldExpression) {
        return Util.makeSubfield(subfieldExpression);
    }


}
