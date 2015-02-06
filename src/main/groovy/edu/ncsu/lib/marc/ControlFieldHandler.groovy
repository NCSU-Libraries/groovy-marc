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
 * Handler that provides implementations of common Groovy methods for MARC control fields (001-008)
 */
class ControlFieldHandler extends FieldHandler {

    ControlFieldHandler(Closure closure, String tag) {
        super(closure,tag)
    }


    ControlField getValue(Record rec) {
        rec.getVariableField(tag)
    }

    def setValue(Record rec, String value) {
        ControlField field = rec.getVariableField(tag)
        if ( field == null ) {
            field = factory.newControlField(tag)
            rec.addVariableField(field)
        }
        field.setData(value)
        field
    }

    def delete(Record rec) {
        rec.getVariableFields(tag).each {
            VariableField fld -> rec.removeVariableField(fld)
        }
    }


}
