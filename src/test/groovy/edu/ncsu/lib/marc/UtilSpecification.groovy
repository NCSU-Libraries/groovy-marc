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

import org.marc4j.marc.Subfield
import spock.lang.Specification

/**
 * Specification for the Util class.
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
