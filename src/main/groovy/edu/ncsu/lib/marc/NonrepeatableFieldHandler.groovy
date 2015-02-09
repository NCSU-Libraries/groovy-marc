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
import org.marc4j.marc.VariableField

/**
 * Methods for working with non-repeatable fields.  When the FieldInfo cache indicates that a field is non-repeatable, it will
 * be assigned this sort of handler, which returns single instances of DataField objects (as opposed to lists).
 * @author ajconsta
 */
class NonrepeatableFieldHandler extends FieldHandler {

    private static def splitter = '[|$]'

    public NonrepeatableFieldHandler(Closure closure, String tag) {
        super(closure, tag)
    }


    DataField getValue(Record rec) {
        def result = (DataField) rec.getVariableField(tag)
        // if we don't actually have a value yet,
        // return a NullDataField object (evaluates to false)
        // that will nonetheless support the leftShift operation
        // to autocreate the field with its subfields.
        if (result == null) {
            result = new NullDataField(rec,tag)

            result.metaClass.autoVivify = {
                Object value ->
                    if (value instanceof List) {
                        if (Util.isListOfStrings((List) value)) {
                            return handleLeftShift(rec, (List<String>) value)
                        } else if (value.every { it instanceof Subfield }) {
                            value.each { Subfield sf ->
                                return addSubfield(rec, sf)
                            }
                        }
                    } else if (value instanceof Subfield) {
                        return addSubfield(rec, (Subfield) value)
                    } else if (value instanceof String) {
                        return addValue(rec, (String) value)
                    }

            }
        }
        return result
    }

    /**
     * Invoked in putAt(String) operations; sets the value of the tag to a single subfield.
     * @param rec
     * @param tagValue
     * @return
     */
    DataField setValue(Record rec, String subfieldExpression) {
        List<VariableField> existingFields = rec.getVariableField(tag)
        if (existingFields) {
            existingFields.each { rec.removeVariableField(it) }
        }
        addValue(rec, subfieldExpression)
    }

    DataField addSubfield(Record rec, Subfield sf) {
        DataField field = (DataField) rec.getVariableField(tag)
        if (field == null) {
            field = Util.makeDataField(tag)
            rec.addVariableField(field)
        }
        field.addSubfield(sf)
        field
    }

    DataField addValue(Record rec, String subfieldExpression) {
        Subfield sf = Util.makeSubfield(subfieldExpression)
        if (sf != null) {
            addSubfield(rec, sf)
        } else {
            throw new IllegalArgumentException("'${subfieldExpression}' is not interpretable as a subfield expression")
        }
        getValue(rec)
    }

    DataField handleLeftShift(Record rec, String value) {
        addValue(rec, value)
        getValue(rec)
    }

    DataField handleLeftShift(Record rec, List<String> tagValues) {
        tagValues.each { val ->
            addValue(rec, val)
        }
        getValue(rec)
    }
}
