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

import java.io.IOException;
import java.io.Writer;

/**
 * A Writer implementation that uses a StringBuilder under the hood.
 */
public class StringBuilderWriter extends Writer {

    /**
     * MARC records in XML format can be long ...
     */
    public static final int DEFAULT_CAPACITY = 38400;

    private StringBuilder builder;

    public StringBuilderWriter(int capacity) {
        builder = new StringBuilder(capacity);
    }

    public StringBuilderWriter() {
        this(DEFAULT_CAPACITY);
    }

    private int usageCount =0;

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        builder.append(cbuf,off,len);
    }

    public String getAndReset() {
        String result = builder.toString();
        builder.setLength(0);
        usageCount++;
        return result;
    }

    public int getUsageCount() {
        return usageCount;
    }


    public StringBuilder getBuilder() {
        return builder;
    }



    @Override
    public void flush() throws IOException {
        // nop

    }

    @Override
    public void close() throws IOException {
        // nop

    }
}
