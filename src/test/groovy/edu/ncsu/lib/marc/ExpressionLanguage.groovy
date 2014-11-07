package edu.ncsu.lib.marc
import org.marc4j.marc.DataField
import org.marc4j.marc.MarcFactory
import spock.lang.Specification
/**
 * Tests for relatively high-level groovy-marc expressions.
 */
class ExpressionLanguage extends Specification {

    def factory = MarcFactory.newInstance()

    def rec = factory.newRecord()

    def setup() {
        rec.leader = factory.newLeader("01414cam  22003138a     ")
    }

    def "fetching a nonexistent field"() {
        when:
        def result = rec['245']
        then:
        result instanceof NullDataField
        result.tag == "245"
    }


    def "assigning to a field via with an ordinary string (no subfield)"() {
        when:
        def result = ( rec['245'] = "whatever" )

        then:
        thrown(IllegalArgumentException)
    }

    def "assigning to a subfield with subfield expression string"() {
        when:
        def result = rec['245'] = '|awhatever'
        then:
        ((DataField)rec.getVariableField("245")).getSubfield('a'.charAt(0)).data == 'whatever'
        rec['245'].getSubfield('a'.charAt(0)).data == "whatever"
    }

    def "assigning to a field with subfield expresion using escaped dollar sign"() {
           when:
           def result = rec['245'] = "\$ai can see my house from here"
           then:
           ((DataField)rec.getVariableField("245")).getSubfield('a'.charAt(0)).data == 'i can see my house from here'
    }

    def "assigning to a nonrepeatable field's subfields via leftShift(List) adds multiple subfields"() {
        when:
        def before = rec['245']
        println("before leftshift: ${before}")
        rec['245'] << ["\$awhatever", "\$aoh wait", "\$athis is bad MARC" ]
        def after = rec['245']
        println("After leftshift: ${after}")
        def title = rec.getVariableFields("245")
        def thing = rec.getVariableField("245")
        then:
        assert before instanceof NullDataField
        assert after instanceof DataField
        thing && (title.size() == 1 )
        title[0].getSubfields('a'.charAt(0)).size() == 3
    }


    def "assigning multiple subfields at once produces one non-repeatable field in the original order"() {
        when:
        println("Starrting leftshift on 710")
        def results  = rec['710'] << ['$bwait', '$bwhat', '$bis this' ]
        println(results)
        then:
        rec['710'] instanceof List
        rec['710'].size() == 1
        rec['710'][0].getSubfields('b'.charAt(0)).collect { it.data } == ['wait', 'what', 'is this']

    }

}
