package edu.ncsu.lib.marc
import org.marc4j.marc.DataField
import org.marc4j.marc.MarcFactory
import org.marc4j.marc.Subfield
import spock.lang.Specification
/**
 * Created by ajconsta on 7/23/14.
 */
class DataFieldExtensionSpecification extends Specification {


    def factory = MarcFactory.newInstance()

    def rec = factory.newRecord()

    def setup() {
        rec.leader = factory.newLeader("01414cam  22003138a     ")
        rec['022'] << [ "|a1234-5678", '$2me' ]
        assert rec.getVariableField("022") != null
        assert rec.getVariableField("022").getSubfield('a'.charAt(0)).data == "1234-5678"
    }

    def "test getting subfields with getAt(string) syntax"() {
        setup:
        def isbn = rec['022'][0]
        def fieldList = isbn['a']
        expect:
        fieldList instanceof List
        fieldList[0] instanceof Subfield
        fieldList[0].data == "1234-5678"
    }

    def "test creating subfields using fromString"() {
        when:
        def x = "|awhatever" as Subfield
        then:
        x instanceof Subfield
        x.code == 'a'.charAt(0)
        x.data == 'whatever'
    }

    def "test setting subfields with putAt(string) syntax"() {
        when:
        def isbn = rec['022'][0]
        def data = 'totallynew'
        isbn['a'] = data
        then:
        isbn.getSubfield('a'.charAt(0)).data == data

    }

    def "check getting indicators using getAt(int) syntax"() {
        setup:
        def isbns = rec['022']
        expect:
        isbns instanceof List
        isbns[0] instanceof DataField

        when:
        def isbn = isbns[0]
        then:
        isbn[1] == Util.BLANK
        isbn[2] == Util.BLANK

        when:
        isbn[3]
        then:
        thrown(IllegalArgumentException)
    }



    def "check setting indicators using putAt(int) syntax"() {
        when:
        def isbn = rec['022'][0]
        isbn[1] = '0'
        then:
        isbn[1] == '0'.charAt(0)
    }
}
