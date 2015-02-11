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

import org.marc4j.marc.ControlField
import org.marc4j.marc.Record
import org.marc4j.marc.VariableField

/**
 * Handler that provides implementations of common Groovy methods for MARC control fields (001-008);
 * getValue() on an 007 field returns a list, while the others return a single ControlField object.
 */
class ControlFieldHandler extends FieldHandler {

    ControlFieldHandler(Closure closure, String tag) {
        super(closure,tag)
    }

    /**
     * @param rec
     * @return
     */
    def getValue(Record rec) {
        return "007".equals(tag) ? rec.getVariableFields(tag) : rec.getVariableField(tag)
    }

    /**
     * Sets the value of the field for a single-vlaue
     * @param rec the record to which the field is being added.
     * @param value the value to set on the field.
     * @return
     */
    def setValue(Record rec, String ... values) {
        if ( "007" == tag ) {
            return setValues(rec[values])
        }
        ControlField field = rec.getVariableFields(tag)
        if ( field == null ) {
            field = factory.newControlField(tag)
            rec.addVariableField(field)
        }
        field.setData(values[0])
        field
    }

    /**
     * Sets multiple values (for the 007 only)
     * @param rec
     * @param values
     * @return
     */
    def setValues(Record rec, List<String> values) {
        rec.getControlFields().each { fld ->
                if ( fld.tag == tag ) {
                    rec.removeVariableField(fld)
                }
        }
        values.each { value ->

            def fld = factory.newControlField(tag,value)
            rec.addVariableField(fld)
        }
        return rec.getVariableFields(tag)
    }

    def delete(Record rec) {
        rec.getVariableFields(tag).each {
            VariableField fld -> rec.removeVariableField(fld)
        }
    }


}
