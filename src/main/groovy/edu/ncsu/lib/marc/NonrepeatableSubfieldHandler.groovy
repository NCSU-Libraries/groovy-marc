package edu.ncsu.lib.marc

/*

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
 * Methods for handling non-repeatable subfields.
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
        println "${tag} : ${subfield} =="
        return df.getSubfield(subfield)
    }


    def read(Record rec) {
        getValue(rec)
    }

    Subfield setValue(Record rec, String value) {
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
