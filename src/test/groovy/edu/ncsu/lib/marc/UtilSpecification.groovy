package edu.ncsu.lib.marc

import org.marc4j.marc.Subfield
import spock.lang.Specification;

/**
 * Created by ajconsta on 7/23/14.
 */
class UtilSpecification extends Specification {



    def "doSubfieldConversion on a mixed bag of objects"() {
        when:

        def subfields = [ "|awhatever" as Subfield, "|bwhatever" as Subfield ]

        def strings = ["whatever", "\$awhatever", "|awhatever" ]
        def testObjects = []
        testObjects.addAll( subfields )
        testObjects.addAll( strings )
        def result = Util.doSubfieldConversion(testObjects)
        then:
        testObjects.size() == subfields.size() + strings.size()
        result.size() == testObjects.size() -1
        result.every { it instanceof Subfield }
    }

    def "testMakeSubfield with dollar sign"() {
        when:
            def result = Util.makeSubfield("\$awhatever")
        then:
            result instanceof Subfield
            result.code == 'a'.charAt(0)
            result.data == "whatever"
    }

    def "makeSubfield with pipe"() {
        when:
        def result = Util.makeSubfield("|awhatever indeed")
        then:
        result instanceof Subfield
        result.code == 'a'.charAt(0)
        result.data == 'whatever indeed'
    }

    def "makeSubfield without subfield indicator"() {
        when:
        def result = Util.makeSubfield("*alet's try this")
        then:
        result == null
    }

    def "makeDataField with control field tag"() {
        when:
            def result = Util.makeDataField("001")
        then:
           thrown(RuntimeException)

    }

    def "makeDataField with tag + indicators"() {
        when:
        def result = Util.makeDataField("245", '1', '2')
        then:
        result.tag == '245'
        result.indicator1 == '1'.charAt(0)
        result.indicator2 == '2'.charAt(0)
    }

    def "makeControlField"() {
        when:
        def result = Util.makeControlField("001")
        then:
        result.tag == '001'
        !result.data
    }
}
