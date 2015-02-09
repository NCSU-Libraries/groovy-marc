package edu.ncsu.lib.marc

/*
 *
     Copyright (C) 2015 North Carolina State University

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    List<Subfield> getValue(Record rec) {
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
