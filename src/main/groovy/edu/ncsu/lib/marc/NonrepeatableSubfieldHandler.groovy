package edu.ncsu.lib.marc

import org.marc4j.marc.DataField
import org.marc4j.marc.Record
import org.marc4j.marc.Subfield

/**
 * Created by ajconsta on 7/22/14.
 */
class NonrepeatableSubfieldHandler extends FieldHandler {

    NonrepeatableSubfieldHandler(Closure closure, String tag, String subfield) {
        super(closure,tag, subfield)
    }


    Subfield getValue(Record rec) {
        DataField df = rec.getVariableField(tag)
        if ( df == null ) {
            return null
        }
        return df.getSubfield(subfield)
    }


    def read(Record rec) {
        getValue(rec)
    }

    Subfield setValue(Record rec, String value) {
        println("setValue(${subfield}, ${rec.getId()}, ${value}")
        DataField df = (DataField)rec.getVariableField(value)
        if ( df == null ) {
            df = factory.newDataField(tag, BLANK, BLANK)
            rec.addVariableField(df)
        }
        Subfield sf = df.getSubfield(subfield)
        if ( sf == null ) {
            sf = factory.newSubfield(subfield)
            df.addSubfield(sf)
        }
        sf.data = value
        sf
    }
}
