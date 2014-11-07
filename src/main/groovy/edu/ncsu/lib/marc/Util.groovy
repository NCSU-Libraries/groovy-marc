package edu.ncsu.lib.marc

import org.marc4j.marc.*

/**
 * Utilities for parsing commonly used expressions and creating fields.
 *
 * @author ajconsta
 */
class Util {


    private static final subfieldPattern = ~'^[|$](.)(.*)$'

    // yeah some of these are not defined, but they don't have subfields, and that's what really matters
    private
    static Set<String> controlFields = Collections.unmodifiableSet(new HashSet<>(["001", "002", "003", "004", "005", "006", "007", "008", "009"]))

    /**
     * Constant char for 'blank' indicators.  null, <code>#</code> and space get converted to this when <em>setting</em> values,
     * but not on <em>getting</em> values if they are somehow set that way.
     */
    public static final char BLANK = ' '.charAt(0)

    private static final MarcFactory mf = MarcFactory.newInstance()

    /**
     * Creates a new subfield from an expression of the form "<code>|[code][data]</code>" or "<code>$[code][data]</code>",
     * where <code>[code]</code> is a single character identifying the subfield type, and <code>[data]</code> is the subfield
     * data.
     * @param expression a shorthand expression for a subfield code and data using the above syntax.
     * @return a subfield with the <code>code</code> and <code>data</code> attributes set by extracting the above
     * values from <code>expression</code>, or <code>null</code> if the expression cannot be parsed.
     */
    static Subfield makeSubfield(String expression) {
        def m = (expression =~ subfieldPattern)
        if (m) {
            return mf.newSubfield(m[0][1].charAt(0), m[0][2])
        }
        println("nope, ya dope: ${expression}")
        null
    }

    /**
     * Create DataField instance with tag and indicators, using just the first character of the
     * indicator arguments.
    *  @param tag the tag for the new field.
     * @param ind1 a string whose first character will be used as the value of ind1
     * @param ind2 a string whose first character will be used as teh value of ind2
     * @return a new datafield.
     */
    static DataField makeDataField( String tag, String ind1, String ind2) {
        makeDataField(tag,ind1.charAt(0), ind2.charAt(0) )
    }

    /**
     * Creates a DataField from a tag and (optionally) indicators.
     * @param tag the tag to use.
     * @param ind1 (default: #BLANK) the value for indicator1
     * @param ind2 (default: #BLANK) the value for indicator2
     * @return
     */
    static DataField makeDataField(String tag, char ind1 = BLANK, char ind2 = BLANK) {
        mf.newDataField(tag, ind1, ind2)
    }

    /**
     * Creates a new control field.
     * @param tag the control field tag.  NOTE: this value is not checked; we are providing the rope, but it's up to you
     * to not hang yourself.
     * @param value (default: empty string) the content of the new field
     * @return
     */
    static ControlField makeControlField(String tag, String value = "") {
        mf.newControlField(tag, value)
    }

    /**
     * Tests whether a tag identifies a control field.
     * @param tag the tag to be tested.
     * @return
     */
    static boolean isControlField(String tag) {
        return controlFields.contains(tag)
    }

    /**
     * Makes a 'default' field of the appropriate type.
     * @param tag the tag for the field to be created.
     * @return a ControlField or a DataField, depending on the value of <code>tag</code>.
     */
    static VariableField makeField(String tag) {
        return isControlField(tag) ? makeControlField(tag) : makeDataField(tag)
    }

    /**
     * Convenience method to add a subfield to a field by expression.  Adds nothing if the subfield
     * expression cannot be parsed.
     * @param field
     * @param subfieldExpression
     * @return <code>null</code> or the added subfield generated from <code>subfieldExpression</code>
     */
    static addSubfield(DataField field, String subfieldExpression) {
        Subfield sf = makeSubfield(subfieldExpression)
        if ( sf ) { field.addSubfield(sf) }
        sf
    }

    static int clearSubfields(DataField field, char code) {
        int result = field.getSubfields().size()
        field.getSubfields(code).each {
            field.removeSubfield(it)
        }
        result
    }

    /**
     * Allows adding a list that might contain String and Subfield objects (anything we know how to c
     * convert into a Subfield)
     * @param field
     * @param values
     * @return
     */
    static List<Subfield> doSubfieldConversion(List values) {
        values.collect { Object value ->
            if ( value instanceof String ) {
                Util.makeSubfield((String)value)
            } else if ( value instanceof Subfield ) {
                (Subfield)value
            } else {
                null
            }
        }.findAll { it }
    }


    /**
     * Creates a Data field from an expression of the form <code>NNN ind1ind2</code>, that is a three-digit tag, followed by a space, followed
     * by two individual characters for the indicators (which can be spaces or <code>#</code> for "undefined" or blank values.
     * @param expr
     */
    static DataField createDataField(String expr) {
        throw new UnsupportedOperationException("need to figure out a syntax for this")
    }

    /**
     * Runtime check to see if an object is a list with only strings in it.  This is used in many <code>leftShift</code> operations
     * to take the appropriate contextual action.
     * @param l the list to be checked.
     * @return
     */
    static boolean isListOfStrings(List l) {
        return l.every { it instanceof String }
    }

    /**
     * Runtime check for list membership.
     * @param l the list to be checked
     * @return <code>true</code> if every member of this list is a list of strings, <code>false</code> otherwise.
     */
    static boolean isLoLOfStrings(List l) {
        try {
            return l.every { it instanceof List && isListOfStrings(it) }
        } catch (ClassCastException ccx) {
            false
        }
    }

    static boolean isListOf(List l, Class<?> clazz) {
        try {
            return l.every { it.class.isAssignableFrom(clazz) }
        } catch( Exception x ) {
            false
        }
    }

}
