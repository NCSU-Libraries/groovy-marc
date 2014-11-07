package edu.ncsu.lib.marc

import org.marc4j.marc.MarcFactory
import org.marc4j.marc.Record
import org.marc4j.marc.VariableField

/**
 * Created by ajconsta on 7/16/14.
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
        println("read called on ${this.class.name}")
        println this.fieldClosure.class.name
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

    /*
    def addFieldValues(rec, values) {
        def returnables = []
        switch (expressionType) {
            case ExpressionType.CONTROL:
                if (values instanceof List) {
                    throw new IllegalArgumentException("${tag} is a control field, you tried to set multiple values")
                }
                def cf = factory.newControlField(tag, values)
                returnables << cf
                rec.addVariableField(cf)
                break
            case ExpressionType.REPEATABLE_FIELD:
                if (!values instanceof List) {
                    values = [values]
                }
                values.each {
                    val ->
                        def field = factory.newDataField(tag, BLANK, BLANK)
                        def sf, sfVal = val.split("\\|")
                        if (sf && sfVal) {
                            field.addSubfield(factory.newSubfield(sf.charAt(0), sfVal))
                        }
                        returnables << field
                        rec.addVariableField(field)
                }
                break
            case ExpressionType.REPEATABLE_SUBFIELD:
                if ( ! ( values instanceof List ) ) {
                    values = [ values ]
                }
                values.each { val ->
                    def field = factory.newDataField(tag, BLANK, BLANK)
                    field.addSubfield(factory.newSubfield((subField as char), val))
                    returnables << field
                    rec.addVariableField(field)
                }
                break
            case ExpressionType.NONREPEATABLE_SUBFIELD:
                def field = factory.newDataField(tag, BLANK, BLANK)
                returnables << field
                field.addSubfield(factory.newSubfield((char)subField, values))
                rec.addVariableField(field)
                break;
            case ExpressionType.NONREPEATABLE_FIELD:
                def field = factory.newDataField(tag, BLANK, BLANK)
                if (!(values instanceof List)) {
                    values = [values]
                }
                returnables = []
                values.each { String val ->
                    field.addSubfield(factory.newSubfield((char)subField, val))
                    returnables << field
                    rec.addVariableField(field)
                }
                break
        }
        returnables
    }                             */


    protected VariableField makeField(String tag) {
        Util.makeField(tag)
    }
}
