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

import org.marc4j.marc.MarcFactory
import org.marc4j.marc.Record
import org.marc4j.marc.VariableField

/**
 * Base class for expressions that work with MARC fields.   FieldHandlers are necessary to provide for
 * expressions that implicitly require different arguments and which return different types of values.
 */
abstract class FieldHandler {

    protected static char BLANK = ' '.charAt(0)

    protected static final MarcFactory factory = MarcFactory.newInstance()

    protected Closure fieldClosure

    protected ExpressionType expressionType

    protected String tag

    protected char subfield


    protected FieldHandler() {

    }


    abstract def getValue(Record rec);


    protected void setSubfield(char subfield) {
        this.subfield = subfield
    }

    protected FieldHandler(Closure closure, String tag) {
        this.fieldClosure = closure
        this.tag = tag
    }

    FieldHandler(Closure closure, String tag, String subfield) {
        this(closure,tag)
        this.expressionType = expressionType
        if ( subfield ) { this.subfield = subfield.charAt(0) }

    }

    def read(Record rec) {
        this.fieldClosure.call(rec, "whatevs")
    }


    private void setFieldValues(fields,values) {

        if ( values instanceof List ) {
            fields.eachWithIndex {
                idx, fld ->
                    if (values.size() > idx) {
                        fld.data = values[idx]
                    }
            }
        } else {
            fields[0].data = values
        }
    }

    protected VariableField makeField(String tag) {
        Util.makeField(tag)
    }
}
