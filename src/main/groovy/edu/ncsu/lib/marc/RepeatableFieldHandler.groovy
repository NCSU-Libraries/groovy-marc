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
 * Handler for repeatable fields; things get a bit complex here, in that
 * left shift operations need to be pretty subtle, e.g.
 * <code>
 *     rec['710'] << [ "b|whatever", "b|this", "b|is" ]
 * </code>
 * will add a single <em>new</em> field with three $b subfields,
 * while the "list of lists"
 * <code>
 *     rec['710'] << [ [ "a|whatever", "b|this", "b|is"],
 *                     [ "a|not", "b|the", "b|same", "b|record" ]
 *                   ]
 * </code>
 * will add *two* new records.
 *
 *
 */
class RepeatableFieldHandler extends FieldHandler {



    def listLeftShift = List.metaClass.&leftShift

    public RepeatableFieldHandler(Closure closure, String tag) {
        super(closure, tag, ' ')
    }


    List<VariableField> getValue(Record rec) {
        def list = rec.getVariableFields(tag) ?: []
        list.metaClass.leftShift = { Object values ->
            if (values instanceof List) {
                List vl = (List) values
                if ( Util.isLoLOfStrings(vl) ) {
                    return addMultipleFields(rec, (List<List<String>>)values)
                } else if ( Util.isListOfStrings(vl) ) {
                    return addFieldWithSubfields(rec, (List<String>)values)
                }

                // if it's a bunch of data fields, then we can try adding them to the record,
                // but let's be sticky about it and only do it if *all* the fields are datafields
                // and just do it for the ones where the tag matches; the Record.leftShift overload
                // will allow adding multiple fields
                if ( values.every { it instanceof DataField } ) {
                    values.each { DataField df ->
                        if ( df.getTag() == tag ) {
                            rec.addVariableField()
                        }
                    }
                }
                return getValue(rec)
            } else if ( ( values instanceof String ) || ( values instanceof GString )) {
                return addFieldSingleSubfield(rec, (String) values)
            } else {
                throw new IllegalArgumentException("Don't know how to add ${values} [${values.class.name}] to a ${tag} field")
            }
        }
        list
    }

    private DataField addValue(Record rec, String value ) {
        addFieldSingleSubfield(rec,value)[0]
    }

    List<VariableField> setValues(Record rec, List<String> values) {
        read(rec).each { VariableField field ->
            rec.removeVariableField(field)
        }
        values.collect { String val ->
            addValue(rec, val)
        }
    }


    DataField addField(Record rec, List<String> subfields) {
        DataField field
        subfields.each { String expr ->
            Subfield sf = Util.makeSubfield(expr)
            if ( sf ) {
                // lazily create and add field; if the return value of this method is null,
                // the user can use that to figure out they messed up.
                if ( field == null ) { field = makeField(tag ); rec.addVariableField(field) }
                field.addSubfield(sf)
            }
        }
        field
    }

    List<DataField> addFieldSingleSubfield(Record rec, String value) {
        addField(rec, [value])
        getValue(rec)
    }

    List<DataField> addFieldWithSubfields(Record rec, List<String> subfields) {
        addField(rec,subfields)
        getValue(rec)
    }

    List<DataField> addMultipleFields(Record rec, List<List<String>> fieldsAndSubFields) {
        fieldsAndSubFields.each { List<String> subfields ->
            addField(rec, subfields)
        }
        getValue(rec)
    }
}
