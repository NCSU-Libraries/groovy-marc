package edu.ncsu.lib.marc;

import groovy.lang.GroovyObjectSupport;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import java.util.Collections;
import java.util.List;

/**
 * An implementation of the NullObject pattern for <code>DataField</code>.
 * Instances of this class are created when using the GroovyMARC expression language to query for fields and the fields
 * do not exist on the record.  Since these queries can be combined seamlessly with assignment operations, we needed a
 * way to automatically create a field of the appropriate type.  This class supports "autovivification" of fields and
 * helps avoid <code>NullPointerException</code>s in Groovy code.
 *
 * <p>
 * Instances of this class evaluate as <code>false</code> in groovy boolean contexts.  Setters are generally no-ops, and
 * getters return null or empty values (where appropriate).  The exception to this rule is <code>getTag()</code> which
 * always returns the originally requested tag value (to aid in autovivification).
 * </p>
 * @author ajconsta
 */
public class NullDataField extends GroovyObjectSupport implements DataField {


    private Record record;

    private String tag;

    public NullDataField(Record record, String tag) {
        this.record = record;
        this.tag = tag;
    }

    /**
     * Always turns false.  Allows easy use of <code>rec['123']</code> in a boolean context to check for existence of a field
     * @return <code>false</code>.
     */
    public boolean asBoolean() {
        return false;
    }


    /**
     * Groovy failsafe to help debug if any instance of this class "leaks through".
     * @param thing the name of the method being invoked.
     * @param args the parameters for the invocation.
     * @return whatever it is that the named method returns.
     */
    @Override
    public Object invokeMethod(String thing, Object args) {
        System.err.printf("NullDataField.%s(%s)", thing, args );
        return super.invokeMethod(thing, args);
    }

    @Override
    public void setId(Long aLong) {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String s) {
        // nop -- this can only be done at object creation time.
    }

    @Override
    public boolean find(String s) {
        return false;
    }

    @Override
    public int compareTo(VariableField o) {
        return 0;
    }

    @Override
    public char getIndicator1() {
        return 0;
    }

    @Override
    public void setIndicator1(char c) {

    }

    @Override
    public char getIndicator2() {
        return 0;
    }

    @Override
    public void setIndicator2(char c) {

    }

    /**
     * Always returns an immutable empty list.
     * @return
     */
    @Override
    public List<Subfield> getSubfields() {
        return Collections.emptyList();
    }

    /**
     * Always returns an immutable empty list.
     * @param c
     * @return
     */
    @Override
    public List<Subfield> getSubfields(char c) {
        return Collections.emptyList();
    }

    /**
     * Counts the subfields, but this is only implemented in the freelib marc4j version.
     * @return
     */
    //@Override
    public int countSubfields() {
        return 0;
    }

    public Subfield getSubfield(char c) {
        return null;
    }

    @Override
    public void addSubfield(Subfield subfield) {

    }

    @Override
    public void addSubfield(int i, Subfield subfield) {

    }

    @Override
    public void removeSubfield(Subfield subfield) {

    }

    /**
     * Added to support autovivification of fields when performing assignment operations in Groovy.
     * @return
     */
    public Record getRecord() {
        return this.record;
    }

}
