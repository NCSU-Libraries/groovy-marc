GroovyMARC Extensions
======================

This project contains a set of Groovy Extensions and utilities that allow for
more "groovyesque" processing of MARC records.  It is based on [marc4j](https://github.com/marc4j/marc4j) and uses that library's facilities to do the heavy lifting.

Background
-----------

MARC records don't really support a path-like expression language for navigation
and manipulation, but they get close enough that I thought I'd give it a shot.
Hence this library.  Other extant MARC processing libraries (e.g. pymarc) make
attempts to support navigating MARC records via normal language constructs.
This is an attempt to do that for Groovy.

I'm not going to try to sell you on Groovy, other than to note that you might
want to run MARC processing scripts and for whatever reason doing this in a
JVM-based environment seems like a good idea to you.  If you're avoiding
writing Java code for this reason and want to use a dynamic language, Groovy
suggests itself.

Unfortunately, Groovy makes dealing with char literals (which are all over the
place in the marc4j API) painful and fussy.  So this project tries to sweep
most of that under the rug.  It also tries to make creating marc4j API objects
pretty simple, e.g. '$athis is my subfield a' can be turned into an appropriate
Subfield object without having to (explicitly) fiddle with marc4j's MarcFactory
class.  You can also use Groovy syntax such as the `leftShift` (`<<`) operator
to quickly add bunches of fields or subfields.

Finally, it adds a bit of syntactic sugar for reading MARC records from disk.

So, mostly, if you like marc4j and you like Groovy, and you need to manipulate
a lot of MARC records and you're looking to maybe reduce the verbosity of the
Java code to do this, you might be interested in kicking the tires on this.

Examples
---------

        // assume 'rec' is a marc4j Record instance
        // get the 245 field
        def titleField =  rec['245']
        // extract the 'a' subfield; this is 'null safe' in that
        // even if the record has no 245, the above query returns
        // a usable object
        def mainTitle = titleField['a'] // titleField['$a'] also works
        // fun with indicators
        def issns = rec['022'] // issns is a (possibly empty) list
        issns.each { issn ->
            // indicators access via integer indexes
            if ( issn[1] == '1' ) { // note that's a char; we return a string
                println "Non-International ISSN: ${issn['a']}"
            }
        }

License
--------

GNU GPL v2.  That's what we can do.

