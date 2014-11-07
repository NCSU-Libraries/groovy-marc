package edu.ncsu.lib.marc

import org.marc4j.marc.DataField
import org.marc4j.marc.Record
import org.marc4j.marc.Subfield

/**
 * Handler for subfields that are repeatable on a non-repeatable field.
 */
class RepeatableSubfieldHandler extends FieldHandler {

    RepeatableSubfieldHandler(Closure clo, String tag, String subfield) {
        super(clo, tag, subfield)
    }


    List<Subfield> getValue(Record red) {
        read(rec)
    }

    def setValue(Record rec, String tagValue) {
       setValues(rec,[tagValue])[0]
    }

    def setValues(Record rec, List<String> values) {
        DataField df = rec.getVariableField(tag)
        if ( df != null ) {
            df.getSubfields(subfield).each {
                Subfield sf -> df.removeSubfield(sf)
            }
        } else {
            df = factory.newDataField(tag, BLANK, BLANK)
            rec.addVariableField(df)
        }
        values.each {
            String data ->
                df.addSubfield(factory.newSubfield(subfield, data))
        }
        read(rec)
    }


}
