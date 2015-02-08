package edu.ncsu.lib.marc

import edu.ncsu.lib.CharUtils
import org.marc4j.marc.DataField
import org.marc4j.marc.Record

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
import org.marc4j.marc.Subfield

/**
 * A set of extensions to the Marc4j <code>DataField</code> class to allow reading and writing it using Groovyesque syntax.
 *
 * <p> Some examples:
 * <pre>
 *  <code>
 *     def x = [ some expression that yields up a DataField object]
 *     def ind1 = x[1] # ind1 will be a char
 *     def ind2 = x[2] # ind2 will be a char
 *     # x[0] IllegalArgumentException; integer index must be 1 or 2
 *
 *     def subfieldA =  x['a'] # either a List<Subfield> or an empty list
 *
 *     def subfieldC = x['$c'] # sim. to above, but we also allow the common $ prefix
 *
 *     def subfield0 = x['0'] # use a string valued index to access subfields instead of indicators
 *
 *     # add a new subfield  $a with some data
 *     x << 'a|the value for subfield A'
 *
 *     x << [ 'a|foo', 'b|bar' ] # adds a $a and a $b subfield to x
 *
 *     def y = [ some expression that evaluates to a Subfield ]
 *
 *     x << y # adds subfield y to x*
 *  </code>
 * </pre>

 * @author ajconsta
 */
class DataFieldExtension {

    /**
     * Add asType(Subfield) to String to allow easier creation of Subfields in groovy with
     * "[subfieldExpression]" as Subfield
     *
     */
    static {
        def defaultStringAsType = String.metaClass.getMetaMethod("asType", [Class] as Class[])
        String.metaClass.asType = { Class type ->
                if (type.isAssignableFrom(Subfield)) {
                    Util.makeSubfield((String)delegate)
                } else {
                    defaultStringAsType.invoke(delegate, [type] as Class[])
                }
        }

        def defaultGSTringAsType = GString.metaClass.getMetaMethod("asType", [Class] as Class[])
        GString.metaClass.asType = { Class type ->
                if ( type.isAssignableFrom(Subfield) ) {
                    Util.makeSubfield(delegate as String)
                } else {
                    defaultGSTringAsType.invoke(delegate, [type] as Class[])
                }
        }
    }

    /**
     * Attempts to add a single subfield from a string expression.
     * @param field
     * @param value
     * @return
     */
    public static Object leftShift(DataField field, String value) {
        field = autoVivifyField (field)
        Util.addSubfield(field, value)
        field
    }


    /**
     * Adds a new subfield to the field using standard Groovy left shift syntax <code>field &lt;&lt; subfield</code>
     * @param field
     * @param subfield
     * @return
     */
    public static Object leftShift(DataField field, Subfield subfield) {
        field = autoVivifyField(field)
        field.addSubfield(subfield)
        field
    }

    /**
      * Adds a list of subfields (string expressions or a Subfield)
      * @param field the target field for the new subfields.
      * @param values a list of candidate subfields.
      * @return the field object.
      * @see Util#makeSubfield(String) for information on formatting subfield expressions.
      */
    public static Object leftShift(DataField field, List subfields) {
        field = autoVivifyField(field)
        Util.doSubfieldConversion(subfields).each {
            field.addSubfield(it)
        }
        field
    }

    /**
     * Override getAt(int) to allow easy access to indicators.
     * @param field the field to be queried
     * @param ind 1 or 2
     * @return the value of the requested indicator.
     * @throws IllegalArgumentException if <code>ind</code> is not <code>1</code> or <code>2</code>
     */
    public static Character getAt(DataField field, Integer ind) {
        if ( ind == 1 ) {
            return field.getIndicator1()
        }
        if (ind == 2 ) {
            return field.getIndicator2()
        }
        throw new IllegalArgumentException("The index in '${field.tag}[${ind}]' can only be 1 or 2 (first or second indicator)")
    }

    /**
     * Override putAt(int) to allow easy setting of indicators.
     * @param field the field to be modified.
     * @param ind the indicator to be modified (must be <code>1</code> or <code>2</code>; any other value will make this a no-op)
     * @param val the value of the indicator; only the first character will be used; <code>null</code>,
     * a space, and <code>#</code> are all evaluated as <code>Util.BLANK</code>.
     * @see Util#BLANK
     */
    public static void putAt(DataField field, Integer ind, String val) {
        char realVal = ( val == ' ' || val == '#' || val == null ) ? Util.BLANK : val.charAt(0)
        if ( ind == 1 ) {
            field.setIndicator1(realVal)

        }
        else if ( ind == 2 ) {
            field.setIndicator2(realVal)

        }
    }

    /**
     * Override getAt(str) to allow groovyesque access to subfields.
     * @param field the field to be queried.
     * @param subfield an expression of a single character indicating the subfield code or a two-character expression of the form <code>$[subfield code]</code>
     * @return a list of subfields on the fields matching the code, which will be empty if there are no matching subfields.  The returned list supports
     * a <code>leftShift</code> (<code>&lt;&lt;) operation that allows adding individual String, Subfield, or List objects with members that can be
     * converted to Subfields.
     */
    public static List<Subfield> getAt(DataField field, String subfield) {
        char code = getSubfieldCode(subfield)
        def result = field.getSubfields(code) ?: []
        result.metaClass.leftShift = {
            Object values ->
                    if ( values instanceof String ) {
                        Util.addSubfield(field,(String)values)
                    } else if ( values instanceof Subfield ) {
                        field.addSubfield((Subfield) values)
                    } else if ( values instanceof List ) {
                        Util.doSubfieldConversion((List)values).each {
                            field.addSubfield(it)
                        }
                    } else {
                        throw new IllegalArgumentException("<< with field[subfield] only works with a single String or Subfield, or a List.")
                    }
        }
        result
    }

    /**
     * Override putAt(str) to allow groovyesque access to setting subfields.  Note that, as a "putAt" operation, this
     * means that any existing subfields with the same code will be removed!
     * @param field
     * @param subfield
     * @return the new subfield
     */
    public static Subfield putAt(DataField field, String subfield, String val) {
        char code = getSubfieldCode(subfield)
        Util.clearSubfields(field, code)
        Subfield sf =  Util.makeSubfield("|${code}${val}")
        field.addSubfield(sf)
        sf
    }



    /**
     * Override putAt(str, List) to allow setting many subfields in a single operation.  Note that this starts off by removing all existing
     * subfields matching <code>subfield</code>.  This provides a means of deleting all subfields by supplying an empty
     * list.
     * @param field
     * @param subfield
     * @param values
     * @return
     */
    public static List<Subfield> putAt(DataField field, String subfield, List values) {
        char code = getSubfieldCode(subfield)
        Util.clearSubfields(code)
        values.each {
            if ( it instanceof String ) {
                Util.addSubfield(field, (String)it)
            } else if ( it instanceof Subfield ) {
                field.addSubfield((Subfield)it)
            }
        }
        field.getSubfields(code)
    }



    public static Boolean isCase(DataField field, String subfield) {
        field.getSubfield(subfield.charAt(0)) != null
    }

    private static char getSubfieldCode(String expr) {
        (char)( expr.charAt(0) == '$' && expr.length() > 1 ? expr.charAt(1) : expr.charAt(0) )
    }

    /**
     * Extends DataField to dynamically add a getSubfield(String) method, making it easier
     * to use the method from within Groovy code.
     * @param field the field.
     * @param subFieldCode a string whose first character will be extracted and passed to
     * DataField.getSubfield(char).
     * @return the first subfield matching (the first character of) <code>subFieldCode</code>
     */
    public static Subfield getSubfield(DataField field, String subFieldCode) {
        return field.getSubfield(CharUtils.toChar(subFieldCode))
    }

    /**
     * Extends DataField to dynamically add a getSubfields(String) method, making it easier
     * to use he method within Groovy code.
     * @param field the field to be accessed
     * @param subFieldCode a string whose first character will be extracted and passed to <code>DataField.getSubfields(char)</code>
     *
     * @return all subfields matching <code>subFieldCode</code>.
     */
    public static List<Subfield> getSubfields(DataField field, String subFieldCode) {
        return field.getSubfields(CharUtils.toChar(subFieldCode))
    }

    /**
     * Automatically "vivifies" a field when it is assigned to.  This allows us to transparently
     * support putAt() and leftShift() operations on fields that aren't actually there.
     * @param field a DataField that may be a mere proxy.
     * @return a DataField that is definitely attached to a Record.
     * @see NullDataField
     */
    private static DataField autoVivifyField(DataField field) {
        DataField theField = field
        if ( field instanceof NullDataField ) {
            Record rec = field.record
            theField = Util.makeDataField(field.tag)
            rec.addVariableField(theField)
        }
        theField
    }



}
