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

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents information about a MARC tag and its subfields (repeatability, etc.).
 */
public class FieldInfo {

	
	private Pattern tagPattern = Pattern.compile("\\d\\d\\d");
	
	private static Set<String> controlFields = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("001", "002", "003", "004", "005", "006", "007", "008", "009")));
	
    private String tag;

    private String description;

    private boolean repeatable = true;

    private Map<String,FieldInfo> subFields = new HashMap<>();

    public String getTag() {
        return tag;
    }
    
    /**
     * Checks whether this tag corresponds to a control field. 
     * @return
     */
    public boolean isControlField() {
    	return controlFields.contains(this.tag);
    }
    
    public Class<?> getMarc4JClass() {
    	if ( tagPattern.matcher(this.tag).matches() ) {
    		return isControlField() ? ControlField.class : DataField.class;
    	}
    	return Subfield.class;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public FieldInfo getSubfield(String subField) {
        return subFields.get(subField);
    }
    
    public FieldInfo getSubfield(char subField) {
    	return subFields.get( String.valueOf(subField) );
    }

    private void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    private void addSubField(String name, boolean repeatable, String description) {
        FieldInfo info = new FieldInfo();
        info.setRepeatable(repeatable);
        info.setDescription(description);
        subFields.put(name, info);
    }

    private void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
    	return this.description;
    }

    public String toString() {
    	return String.format("MARC tag info [%s]", this.tag);
    }

    public static class FieldInfoBuilder {

        private FieldInfo f;

        public FieldInfoBuilder(String tag) {
            f = new FieldInfo();
            f.tag = tag;
        }

        public FieldInfo build() {
            return f;
        }

        /**
         * Adds a non-repeatable subfield.
         * @param code the subfield code.
         * @param description
         * @return
         */
        public FieldInfoBuilder addSubfield(String code, String description) {
            f.addSubField(code,false, description);
            return this;
        }

        public FieldInfoBuilder makeRepeatable() {
            f.setRepeatable(true);
            return this;
        }

        public FieldInfoBuilder makeNonRepeatable() {
            f.setRepeatable(false);
            return this;
        }

        public FieldInfoBuilder addRepeatableSubfield(String code, String description) {
            f.addSubField(code,true,description);
            return this;
        }

    }
}
