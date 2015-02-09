package edu.ncsu.lib.marc

import spock.lang.Specification

/**
 * Created by adamc on 2/8/15.
 */
class ExpressionParserSpecification extends Specification {



    def "test that field expressions are processed correctly"() {
        given:
            def p = new ExpressionParser()
        expect:
            exprClass.equals( p.parse(expr).getClass() )

        where:
            expr    | exprClass
            "045"   | RepeatableFieldHandler.class
            "245"   | NonrepeatableFieldHandler.class
            '245$a' | NonrepeatableSubfieldHandler.class
            "988|a" | RepeatableSubfieldHandler.class
    }


}
