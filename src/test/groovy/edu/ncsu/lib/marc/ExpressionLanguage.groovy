package edu.ncsu.lib.marc

import org.marc4j.marc.ControlField

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
            def results  = rec['710'] << ['$bwait', '$bwhat', '$bis this' ]
        then:
            rec['710'] instanceof List
            rec['710'].size() == 1
            rec['710'][0].getSubfields('b'.charAt(0)).collect { it.data } == ['wait', 'what', 'is this']

    }

    def "assigning multiple values to a nonrepeatable control field produces one control field"() {
        when:
            rec['003'] = 'NcRS'
        then:
            assert rec['003'] instanceof ControlField
            assert rec['003'].data == "NcRS"

    }

    def "assigning to the 007 yields a list of control fields"() {
        when:
            rec['007'] = "whatever"
        then:
            assert rec['007'] instanceof List
            assert rec['007'].size() == 1
            assert rec['007'][0].data == "whatever"
    }

}
