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

/**
 * Enumerated type for MARC 'path' expressions supported by this extension.
 */
public enum ExpressionType {
    CONTROL,
    // repeatable field
    REPEATABLE_FIELD,
    NONREPEATABLE_FIELD,
    // repeatable subfield of repeatable OR non-repeatable subfield
    REPEATABLE_SUBFIELD,
    // non-repeatable subfield of non repeatable field
    NONREPEATABLE_SUBFIELD

}
