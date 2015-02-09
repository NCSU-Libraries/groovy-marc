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
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.marc4j.marc.*

import static edu.ncsu.lib.CharUtils.toChar

import java.util.concurrent.ConcurrentHashMap

/**
 * Converts expressions for MARC fields and subfields into the appropriate closures.
 *
 * TODO : add more interesting documentation and behavior based on field
 * definitions (e.g. non-repeatable fields should not have "listy" behavior unless
 * operating in non-strict mode))
 *
 * @see RecordExtension
 */
@Slf4j
class ExpressionParser {

	// expermient: if we know which fields and subfields are repeatable, we can compile different closures for
	// expressions containing them.
	private Map<String, FieldInfo> _fieldInfoCache = new ConcurrentHashMap<String, FieldInfo>()


    public ExpressionParser() {
        _fieldInfoCache["245"] = new FieldInfo.FieldInfoBuilder("245")
                    .addSubfield("a", "Title")
                    .addSubfield("b", "Remainder of title")
                    .addSubfield("c", "Statement of Responsibility")
                    .addSubfield("f", "Inclusive dates")
                    .makeNonRepeatable()
                    .build();
        _fieldInfoCache['035'] = new FieldInfo.FieldInfoBuilder("035")
            .makeRepeatable()
            .addSubfield("a", "System control number")
            .addRepeatableSubfield("z", "Canceled/invalid control number")
            .addSubfield("6","Linkage")
            .addSubfield("8","Field link and sequence number")
            .build();
    }
				

    public FieldInfo getFieldInfo(String tag) {
		if ( !_fieldInfoCache.containsKey(tag) ) {
			_fieldInfoCache[tag] = new FieldInfo.FieldInfoBuilder(tag).build();
		}
		return _fieldInfoCache[tag]
    }


    private static final Closure makeRepeatableFieldClosure(String tag, String subField) {

		def c = subField != null && subField.length() > 0 ? toChar(subField) : ''
        return { Record rec, stuff ->
            def result = []
	        def fields = rec.getVariableFields(tag)

           if ( c ) {
            	fields.each { VariableField fld ->
					if ( fld.metaClass.respondsTo(fld, "getSubfields") ) {
						result.addAll( ((DataField)fld).getSubfields(c).collect { Subfield it -> it } )
					}
				}
            } else {
        		result = fields
            }
            return result
        }
    }
	
	private static final Closure<ControlField> makeControlFieldClosure(String tag) {
		return {
			Record rec, String stuff ->
				(ControlField)rec.getVariableField(tag)
		}	
	}

    private static final Closure makeNonRepeatableFieldClosure( String tag ) {
        return { Record rec, String path ->
            log.trace("NRFC (no subfield)")
            DataField df = rec.getVariableField(tag)
            df != null ?:  new NullDataField()
        }
    }

    private static final Closure<Subfield> makeNonRepeatableFieldClosure(String tag, String subField) {
	    def c = toChar(subField)
        return { Record rec, String path ->
            DataField fld = (DataField)rec.getVariableField(tag)
            if ( fld != null ) {
                return fld.getSubfield(c)
            }
            return null
        }
    }


	@Memoized
    public FieldHandler parse(String expression) {
        def (field, subField) = expression.tokenize('|$')
		log.trace("Evaluating expresssion <${expression}> : [${field}] =>[${subField}]")
        FieldInfo info = getFieldInfo(field)

		if ( Util.isControlField(field) ) {
			return new ControlFieldHandler(makeControlFieldClosure(field), field)
		}

        boolean hasSubfield = subField != null && subField.length() == 1 && subField != ' '
        char theSubfield = hasSubfield ? toChar(subField) : ' '

        if ( info.repeatable ) {
            if ( hasSubfield ) {
                def clo = makeRepeatableFieldClosure((String)field,subField)
                return new RepeatableSubfieldHandler(clo, field, subField)
            } else {
                return new RepeatableFieldHandler(makeRepeatableFieldClosure((String)field, ''), (String)field)
            }

        } else {
            if ( hasSubfield ) {
                if ( info.getSubfield(theSubfield).repeatable ) {
                    return new RepeatableSubfieldHandler(makeRepeatableFieldClosure(field, subField), field, subField)
                } else {
                    return new NonrepeatableSubfieldHandler(makeNonRepeatableFieldClosure(field, subField), field, subField)
                }
            }
            return new NonrepeatableFieldHandler(makeNonRepeatableFieldClosure(field), field)
        }

    }
}
