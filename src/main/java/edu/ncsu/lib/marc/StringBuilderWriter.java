package edu.ncsu.lib.marc;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by ajconsta on 8/4/14.
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
