package edu.ncsu.lib.marc


class ReaderFactory {
	
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
        assert ReaderFactory.newReader("")
    }
}
