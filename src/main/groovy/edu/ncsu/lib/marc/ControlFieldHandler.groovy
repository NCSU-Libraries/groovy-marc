package edu.ncsu.lib.marc

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
