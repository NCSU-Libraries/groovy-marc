# Groovy MARC Extensions

This project contains a set of Groovy Extensions and utilities for [marc4j](https://github.com/marc4j/marc4j)
that allow for more "groovyesque" processing of MARC records.

## Background

MARC records don't really support a path-like expression language for navigation
and manipulation, but they get close enough that I thought I'd give it a shot.

Other extant MARC processing libraries (e.g. pymarc) make
attempts to support navigating MARC records via normal language constructs.
This is an attempt to do that for Groovy.

I'm not going to try to sell you on Groovy, other than to note that you might
want to run MARC processing scripts and for whatever reason doing this in a
JVM-based environment seems like a good idea to you.  If you're avoiding
writing Java code for whatever reason and want to use a dynamic language, Groovy
suggests itself as an option (well, I suggest Groovy as an option).

Despite its general strengths in Java integration, Groovy makes dealing with `char`
literals (which are all over the place in the marc4j API) painful and fussy.
So this project tries to sweep most of that under the rug.  It also tries to make creating
marc4j API objects pretty simple, e.g. `$athis is my subfield a` can be turned into an appropriate
Subfield object without having to (explicitly) fiddle with marc4j's `MarcFactory`
class.  You can also use Groovy syntax such as the `leftShift` (`<<`) operator
to quickly add bunches of fields or subfields.

Finally, it adds a bit of syntactic sugar for reading MARC records in a groovesque fashion
(think: `marcReader.each { rec -> ... }`), although currently this
is only available when the library is compiled under Java 8.


### Examples
```

        // assume 'rec' is a marc4j Record instance
        // get the 245 field
        def titleField =  rec['245']
        // extract the 'a' subfield; this is 'null safe' in that
        // even if the record has no 245, the above query returns
        // a usable object so this will not fail outright
        def mainTitle = titleField['a'] // titleField['$a'] also works

        // or, even faster
        def stitle = rec['245$a']
         // equivalent to the above
        def ptitle = rec['245|a']

        // fun with indicators
        def issns = rec['022'] // issns is a (possibly empty) list
        issns.each { issn ->
            // issn is a DataField instance
            // note that above we accessed a subfield using a string ('a')
            // if we use an integer that's 1 or 2, we get the value of the first
            // or second indicator
            // indicators access via integer indexes
            if ( issn[1] == '1' ) {
                // note that '1' is a char in marc4j
                // in this API we return a string
                println "Non-International ISSN: ${issn['a']}"
            }
        }
```

### Usage

Compile it (the build system is [Gradle](http://www.gradle.org), and put the resulting jar
(output into `build/libs/groovy-marc-${VERSION}.jar`) on your classpath.

If you compile under a JDK prior to Java 8, the `edu.lib.ncsu.marc.Reader` and `edu.lib.ncsu.marc.RecordSpliterator` classes
will not be available in the output (these enable reading MARC records through the Streams API introduced in Java 8),
and the output jar will have the "jdk7" classifier added to its name.  If there is interest, I'll add a Reader implementation for
Java 7.

The extensions are loaded and immediately available
to any of your Groovy code, without further intervention on your part, e.g. no matter how you get your hands
on a marc4j `Record` instance, the extension methods can be invoked on it.

Oh and, read the groovydocs for full documentation of the APIs, and look at the Specification classes under `src/test/groovy` for sample
operations.

-- Adam


### License

GNU GPL v3.  (c) 2015 North Carolina State University.



