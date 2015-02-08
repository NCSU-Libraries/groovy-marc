package edu.ncsu.lib.marc


class ReaderFactory {

    /**
     * Creates a new Iterable MARC record reader.
     * @param input a File, String, or InputStream from which records are to be read.  If a String, it will
     * be interepreted as a relative file path.
     * @param format the format of the input (usually only necessary if <code>input</code> is an InputStream, as
     * the File and String formats interpret ".xml" extensions as MARCXML).
     * @param encoding the encoding of the input file (again, only necessary for InputStream).
     * @return an Iterable, AutoCloseable reader over the MARC records available from the input.
     */
	static def newReader(Object input, Format format = Format.MARC21, String encoding = "") {
		try {
			def streamReaderClass = Class.forName("edu.ncsu.lib.marc.GroovyMarcStreamerReader")
            println streamReaderClass.constructors
			return streamReaderClass.getConstructor( [ Object, Format, String ] as Class[] ).newInstance(input,format,encoding)
		} catch ( ClassNotFoundException cnfe ) {
			// running under JDK 7
			def iteratorReaderClass = Class.forName("edu.ncsu.lib.marc.GroovyMarcReader")
			return iteratorReaderClass.getConstructor( [ Object, Format, String] as Class [] ).newInstance(input,format,encoding)
		}
	}


    public static void main(String [] args) {
        assert ReaderFactory.newReader(args[0])
    }
}
