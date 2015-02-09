package edu.ncsu.lib.marc;

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

import org.marc4j.marc.Subfield;

/**
 * Extension class to support operations on subfields.
 */
public class SubfieldExtension {


    /**
     * Extension to allow appending data to a subfield's value.
     * @param subfield the subfield.
     * @param value the data to be appended.
     * @return the value of the data after the append.
     */
    public static String leftShift(Subfield subfield, String value) {
        subfield.setData( subfield.getData() + value );
        return subfield.getData();
    }

    public static Subfield fromString( Subfield subfield, String subfieldExpression) {
        return Util.makeSubfield(subfieldExpression);
    }

}
