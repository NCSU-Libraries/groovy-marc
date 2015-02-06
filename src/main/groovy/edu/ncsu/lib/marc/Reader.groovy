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
 * MARC reader extension that implements Iterable and adds methods to marc4j's Record class to make it a bit easier to write
 * simple processors for records.
 * <p>
 *     <b>NB</b> JDK 8+only.
 * </p>
 */
@Slf4j
class Reader implements Iterable<Record>, Iterator<Record>, AutoCloseable {

    enum Format {
        MARC21,
        XML;
    }

    private MarcReader reader

    private InputStream inputStream

    def Reader(source,format = Format.MARC21, encoding = "" ) {
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
        } else if ( source instanceof File ) {
            File input = (File) source
            inputStream = input.newInputStream()
            if ( input.name.endsWith(".xml") || format == Format.XML ) {
                reader = new MarcXmlReader(inputStream)
            } else {
                if ( encoding ) {
                    reader = new MarcStreamReader(inputStream, encoding)
                } else {
                    reader = new MarcStreamReader(inputStream)
                }
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
       /*Properties moduleProps = new Properties()
        moduleProps.setProperty("moduleName", "groovyMarc")
        moduleProps.setProperty("moduleVersion", "1.0")
        moduleProps.setProperty("extensionClasses", "edu.ncsu.lib.marc.RecordExtension,edu.ncsu.lib.marc.DataFieldExtension,edu.ncsu.lib.marc.SubfieldExtension")
        def module = MetaInfExtensionModule.newModule( moduleProps, Reader.class.getClassLoader() )
        module.instanceMethodsExtensionClasses.each {
            println it
        }
        */

		def r = new Reader( new File("/Users/ajconsta/Documents/workspace/symphony-ole-migrator/data/sample.mrc").newInputStream() )
        r.stream().forEach {
            println it.leader
   			println "001: " + it['001']
   			println "ControlNumber: " + it.controlNumber
   			println "918\$a: " +it["918|a"]
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
