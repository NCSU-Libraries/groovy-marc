package edu.ncsu.lib.marc
import org.marc4j.marc.MarcFactory
import spock.lang.Specification
/**
 * Created by ajconsta on 7/23/14.
 */
class RecordExtensionSpecification extends Specification {

    def factory = MarcFactory.newInstance()

        def rec = factory.newRecord()

        def setup() {
            rec.leader = factory.newLeader("01414cam  22003138a     ")
            rec['022'] << [ "|a1234-5678", '$2me' ]
            assert rec.getVariableField("022") != null
            assert rec.getVariableField("022").getSubfield('a'.charAt(0)).data == "1234-5678"
        }


    def "test 'in' operator isCase(String) to check for availability of tag"() {
        expect:'022' in rec
    }

    def "test 'in' operator with subfield expression"() {
        expect:
        '022$a' in rec
        '022|a' in rec
    }
}
