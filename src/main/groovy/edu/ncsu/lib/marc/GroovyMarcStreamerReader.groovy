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

import java.util.function.Consumer
import java.util.stream.Stream
import java.util.stream.StreamSupport
/**
 * Decorator for marc4j MarcReader that adds iterability, auto-closeability, and JDK 8 Stream compatibility
 * to the underlying reader.
 * <p>
  *     Sample usage:
  *     <pre>
  *     <code>
  *         def r = new GroovyMarcReader( new File("sample.mrc"), encoding = "utf-8")
  *         r.each {
  *          Record r ->
  *              ...
  *         }
  *
  *      </code>
  *     </pre›
 *      or (using Streams):
 *      <pre>
   *     <code>
   *         def r = new GroovyMarcReader( new File("sample.mrc"), encoding = "utf-8")
   *         r.stream().filter( { Record rec -> rec["001"] }).collect()
   *      </code>
   *     </pre›
  *
  * </p>
 */
@Slf4j
class GroovyMarcStreamerReader implements Iterable<Record>, Iterator<Record>, AutoCloseable {

    private MarcReader reader

    private InputStream inputStream

    def GroovyMarcStreamerReader(Object source,Format format = Format.MARC21, String encoding = "" ) {
        if ( source instanceof InputStream ) {
            this.inputStream = (InputStream) source
            switch (format) {
                case Format.MARC21:
                    if (encoding) {
                        this.reader = new MarcStreamReader(this.inputStream, encoding)
                    } else {
                        this.reader = new MarcStreamReader(inputStream)
                    }
                    break
                default:
                    this.reader = new MarcXmlReader(inputStream)
            }
        } else if ( source instanceof String || source instanceof GString ) {
            buildReaderFromFile( new File(source), format, encoding)
        } else if ( source instanceof File ) {
            buildReaderFromFile((File)source, format, encoding)
        }
        assert reader != null, "Couldn't build a reader from ${ source == null ? 'null object' : source.getClass().name}"
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
	Spliterator<Record> spliterator() {
		return new RecordSpliterator(this)
	}

    @Override
    void remove() {
        throw new UnsupportedOperationException("Remove() not supported on this class")
    }
	
	public static void main(String [] args ) {
		def r = new GroovyMarcStreamerReader( new File( args[0]).newInputStream() )
        r.stream().forEach {
            println it.leader
   			println "001: " + it['001']
   			println "ControlNumber: " + it.controlNumber
   			println "245\$a: " +it["245|a"]
        }
	}

	@Override
	public void forEachRemaining(Consumer<? super Record> action) {
		Objects.nonNull(action)
		while( hasNext() ) {
			action.accept(next())
		}
	}

    /**
     * Get the contents of this reader as a Stream.
     * @return
     */
    public Stream<Record> stream() {
        return StreamSupport.stream( Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false);
    }

	@Override
	public void forEach(Consumer<? super Record> action) {
		Objects.nonNull(action)
		while( hasNext() ) {
			action.consume(next())
		}
		
	}

    @Override
    public void close() throws Exception {
        this.inputStream.close();
    }
	
}
