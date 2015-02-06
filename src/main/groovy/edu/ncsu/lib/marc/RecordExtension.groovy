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

import groovy.xml.MarkupBuilder
import org.marc4j.Constants
import org.marc4j.marc.ControlField
import org.marc4j.marc.DataField
import org.marc4j.marc.Record
import org.marc4j.marc.Subfield
/**
 * Extension for Marc4J's <code>Record</code> class that allows accessing fields and subfields via groovy-esque syntax.
 * <p>
 *     For example, <code>rec['245$a']</code> returns the 245 subfield a (and is null-safe; if there is no 245 field
 *     the returned object is a
 */
public class RecordExtension {



    private static Map converters = ['a': { it } ]

    private static def makeConverter() {
        def converters = [ "info.freelibrary.marc4j.converter.impl.AnselToUnicode",

            "org.marc4j.converter.impl.AnselToUnicode" ]
            def avail = converters.collect {
                String className ->
                    try {
                        Class<?> clazz = Class.forName(className)
                        try {
                            return clazz.newInstance( [true ])
                        } catch (Exception e ) {
                            return clazz.newInstance()
                        }
                    } catch ( ClassNotFoundException cnfe ) {
                        null
                    }
            }

            avail.find { it }
    }

    /**
     * ThreadLocal to hold MarkupBuilder objects in a threadsafe fashion.  The Builders are configured
     * with multibyte AnselToUnicode converters and custom writers that output to a reusable StringBuilderWriter
     * class.
     * @see #toXML(Record rec) for usage example.
     */
    private static final ThreadLocal<MarkupBuilder> xmlBuilders = new ThreadLocal<MarkupBuilder>() {
        @Override
        public MarkupBuilder initialValue() {

            def converter = makeConverter()
            converters.' ' = { String data -> converter.convert(data) }
            def writer = new StringBuilderWriter()

            MarkupBuilder builder = new MarkupBuilder(writer)

            builder.metaClass.getWriter = {
                writer;
            }

            builder.metaClass.getConverter = { converter }
            builder
        }
    }

    private static ExpressionParser parser = new ExpressionParser()

    /**
     * Allows accessing fields and subfields with Groovy's "index" syntax.
     * @param rec the record to be accessed.
     * @param path an expression denoting the field/subfield to be accessed.
     * @return
     */
    public static Object getAt(Record rec, String path) {
        FieldHandler result = parser.parse(path)
        result.getValue(rec)
    }

    public static Object getProperty(Record rec, String path) {
        return getAt(rec,path)
    }

    public static Object leftShift(Record rec, String path, String value) {
        FieldHandler handler = parser.parse(path)
        if (handler.respondsTo("handleLeftShift")) {
            return handler.handleLeftShift(rec, value)
        }

        handler.setValue(rec, value)
    }

    public static Object leftShift(Record rec, String path, List<String> expressions) {
        FieldHandler handler = parser.parse(path)
        if (handler.respondsTo("handleLeftShift")) {
            return handler.handleLeftShift(rec, expressions)
        }
        handler.addFieldValues(rec, expressions)
    }


    public static Object putAt(Record rec, String path, Object value) {
        FieldHandler handler = parser.parse(path)
        if ( handler.respondsTo("setValues") ) {
            // supports multiple assignments
            handler.setValues(rec, value instanceof List ?: [value] )
        } else {
            // no multiple assignment; getValue() might return null!
            handler.setValue(rec, value instanceof List ? value[0] : value)
        }
    }

    public static Record leftShift(Record rec, ControlField field) {
        rec.controlFields.each {
            ControlField fld ->
                if (fld.tag == field.tag) {
                    rec.removeVariableField(fld)
                }
        }
        rec.addVariableField(field)
        rec
    }

    /**
     * Allows adding a DataField instance to the record.
     * @param rec
     * @param dfld the datafield to be added.
     * @return the record
     */
    public static Record leftShift(Record rec, DataField dfld) {
        rec.addVariableField(dfld)
        rec
    }

    public static Boolean isCase(Record rec, String tagExpression) {
        return getAt(rec, tagExpression) ? true : false
    }

    public static String toXML(Record rec) {
        def builder =  xmlBuilders.get()
        def converter = converters[String.valueOf(rec.leader.getCharCodingScheme())]

        builder.collection(xmlns: Constants.MARCXML_NS_URI ) {
            record() {
                leader(rec.leader.toString())
                rec.controlFields.each {
                    controlfield(tag:it.tag, it.data)
                }
                rec.dataFields.each { df ->
                    datafield(tag:df.tag,ind1:df.indicator1, ind2:df.indicator2) {
                        df.subfields.each { Subfield sf ->

                        subfield(code:sf.code, converter ? converter(sf.data) : sf.data )
                        }
                    }
                }
            }
        }
        builder.writer.andReset
    }

}
