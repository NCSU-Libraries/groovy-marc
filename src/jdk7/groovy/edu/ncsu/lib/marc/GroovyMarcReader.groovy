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
import groovy.util.logging.Slf4j
import org.marc4j.MarcReader
import org.marc4j.MarcStreamReader
import org.marc4j.MarcXmlReader
import org.marc4j.marc.Record

/**
 * Decorator for marc4j MarcReader classes that adds iterabilityb (and thus all the standard Groovy collection
 * methods) and auto-closeability to the underlying reader.  This implementation does not have the JDK 8 Streams
 * functionality in <code>GroovyMarcStreamerReader</code>, but is otherwise compatible.
 * <p>
 *     Sample usage:
 *     <pre>
 *     <code>
 *         def r = new GroovyMarcReader( new File("sample.mrc"), encoding = "utf-8")
 *         r.each {
 *          Record r ->
 *              ...
 *         }
 *      </code>
 *     </pre›
 * </p>
 **/
@Slf4j
class GroovyMarcReader implements Iterable<Record>, Iterator<Record>, AutoCloseable {

    private MarcReader reader

    private InputStream inputStream

    /**
     * Creates an instance of this class.
     * @param source an <code>InputStream</code> or <code>File</code> for the MARC records.
     * @param format the format of the underlying stream.  This parameter is most useful if
     * <code>source</code> is an <code>InputStream</code>, since the <code>File</code> constructor can
     * inspect the file's extension (defaults to MARC21 unless extension is <code>.xml</code>).
     *
     * @param encoding (only necessary for <code>MARC21</code> format, as <code>MarcXmlWriter</code> will
     * autodetect encoding.  If unset, you get marc4j's default encoding.
     */
    def GroovyMarcReader(Object source,Format format = Format.MARC21, String encoding = "" ) {
        if ( source instanceof InputStream ) {
            this.inputStream = (InputStream)source
            switch(format) {
                case Format.MARC21:
                    if ( encoding ) {
                        this.reader = new MarcStreamReader(this.inputStream,encoding)
                    } else {
                        this.reader = new MarcStreamReader(inputStream)
                    }
                    break
                default:
                    this.reader = new MarcXmlReader(inputStream)
            }
        } else if ( source instanceof String || source instanceof GString  ) {
            buildReaderFromFile(new File(source), format, encoding)
        } else if ( source instanceof File ) {
            buildReaderFromFile((File)source,format,encoding)
        }
    }

    private void buildReaderFromFile(File input, Format format = Format.MARC21, String encoding = "") {
            InputStream inputStream = input.newInputStream()
            if (  input.name.endsWith(".xml") ) {
                reader = new MarcXmlReader(inputStream)
            } else {
                if ( encoding ) {
                    reader = new MarcStreamReader(inputStream,encoding)

                } else {
                    reader = new MarcStreamReader(inputStream)
                }
            }
        }

    @Override
    Iterator<Record> iterator() {
        return this;
    }

    @Override
    boolean hasNext() {
        return reader.hasNext()
    }

    @Override
    Record next() {
            return reader.next()
        //return new GRecord(reader.next())
    }
	
    @Override
    void remove() {
        throw new UnsupportedOperationException("Remove() not supported on this class")
    }
	
	public static void main(String [] args ) {
		def r = new GroovyMarcReader( args[0] )
        	r.stream().forEach {
            		println it.leader
   			println "001: " + it['001']
   			println "ControlNumber: " + it.controlNumber
   			println "918\$a: " +it["918|a"]
        	}
	}

    @Override
    public void close() throws Exception {
        this.inputStream.close();
    }
	
}
