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

Finally, it adds a factory class `edu.ncsu.lib.marc.ReaderFactory` that can produce decorated marc4j `MarcReader` instances, allowing "groovyesque" methods of
reading, modifying, and collecting MARC records (e.g.  

```
def reader = ReaderFactory.newReader( new File("mymarc.mrc") )
reader.each { ... }
```

The `ReaderFactory.newReader` method produces `java.util.Iterable` readers that
support streams when it is compile under Java 8, or which does not when
compiled under Java 7.  The output artifact includes the "jdk7" classifier when
compiled under JDK 7.

### Examples

In general, the idea is to allow access to the fields and subfields of a MARC rerecord using standard groovy dictionary-style expressions, e.g. `record["245"]` returns all the 245 fields on the record, while `record["245|a"]` or, equivalently, `record["245\$a"]` fetch all the subfield 'a's of all the 245 fields.  Note that the pipe character is supported in addition to the more commonly-used $, due to the latter having special meaning in Groovy string expressions (if you really like using '$' unescaped, use single quotes in your expressions).

Manipulating records also follows this general syntax, e.g. `record["245"] << "|aThe worst story never told"` adds a new subfield 'a' to the 245 field.

Additionally, existence checks for fields are "booleanized" in the way one should expect, so you can check for the existence of a `245$a` with an expresison like `if ( record["245|a"] ) { } else { println "Record has no 245\$a!" }`

As a final convenience, the "NullDataField" produced when a field expression does not match the record supports "autovivification", so you can *set* the 245 subfield a to your preferred value via the expression `record["245|a"] = "The Only Title You Will Ever Need"`, even if there was originally no 245 on the record.

(well, that last bit made sense to me, I was using this to clean up a bunch of records that didn't have all the required fields).

Other Examples:

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

Compile it (the build system is [Gradle](http://www.gradle.org), and put the
resulting jar
(output into `build/libs/groovy-marc-${VERSION}.jar`) on your classpath.

Once you do this, the extensions are loaded and immediately available
to any of your Groovy code, without further intervention on your part, e.g. no
matter how you get your hands on a marc4j `Record` instance, the extension
methods can be invoked on it.

Currently there is no usage guide, but if you look at the source code of the
tests under `src/test/groovy`, you'll see some sample use patterns.

-- Adam

### License

GNU GPL v3.  (c) 2015 North Carolina State University.

