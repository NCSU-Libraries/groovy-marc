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
import org.marc4j.marc.DataField
import org.marc4j.marc.MarcFactory
import org.marc4j.marc.Subfield
import spock.lang.Specification
/**
 *  Unit tests (and samples) for marc4j DataField-related operations.
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

    def "putAt with repeatable subfield results in single value"() {
        when:
            assert !rec['988|a']
            rec['988|a'] = "new stuff"
        then:
            rec['988|a'] instanceof List
            rec['988|a'][0] instanceof Subfield
            rec['988|a'][0].data == "new stuff"
    }



    def "check setting indicators using putAt(int) syntax"() {
        when:
        def isbn = rec['022'][0]
        isbn[1] = '0'
        then:
        isbn[1] == '0'.charAt(0)
    }


}
