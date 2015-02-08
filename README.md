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
want to run MARC processing scripts and let's assume that doing
JVM-based environment seems like a good idea to you (peformance, environment constraints, etc.).

If you're nonetheless avoiding
writing Java code or are worried that it will be too verbose, then  Groovy
suggests itself as an option (well, I suggest Groovy as an option).

The problem with just importing marc4j and proceeding in Groovy is that, *despite* its '
general strengths in Java integration, Groovy makes dealing with `char`
literals (which are all over the place in the marc4j API) painful and fussy.  Moreover, going from fluid
dynamic groovy code style to Java code-style can be a bit jarring.

So this project tries to sweep most of that under the rug.  It also tries to make creating
marc4j API objects pretty simple, e.g. `$athis is my subfield a` can, in the right contexts, be turned into an appropriate
Subfield object without having to (explicitly) fiddle with marc4j's `MarcFactory`
class.  You can also use Groovy syntax such as the `leftShift` (`<<`) operator
to quickly add bunches of fields or subfields.

Finally, it adds a factory class `edu.ncsu.lib.marc.ReaderFactory` that can produce decorated marc4j `MarcReader`
instances, allowing "groovyesque" methods of reading, modifying, and collecting MARC records (e.g.

```
def reader = ReaderFactory.newReader( new File("mymarc.mrc") )
reader.each { ... }
```

If you compile this library using JDK 8, you can use the Streams API with the reader returned by the factory,
including auto-parallelizing processing, Java 8 lambdas, and things like that.  When you use JDK 7, the reader
implements the older Iterable interface without streams (and the name of the artifact generated will include the "jdk7"
classifier in this case).

### Examples

In general, the idea is to allow access to the fields and subfields of a MARC
record using standard groovy map-style expressions, e.g.

- `record["245"]` returns all the 245 fields on the record as a list (yes, even though it's non-repeatable)
- `record['245']['a']` - returns the 245$a subfield (as a list)
- `record['245'][1]` - (note integer literal here in second index) gets the value of the first indicator.
- `record["245|a"]` - returns the 245 subfield 'a' values; this is equivalent to the second example above.
- `record['245']` - supports a "boolean" interpretation, i.e. the expression can be used to check for the presence of a field.

Note that '|' and '$' are both valid subfield delimiters in an expression, but '$' in a *double-quoted* Groovy string
indicates interpolation (that is: "245$a" means roughly 'the literal 245 with the Stringified value of the variable
'a'"), so you will either have to use single quoted strings or escape the '$' to use the more common form, e.g. the
above expression is equivalent to both `record['245$a']` and `record["245\$a"]`.

Note that these expressions tend to return lists, so if you want to look at all the 035s,(e.g.) you would do something like `record["035"].each {
    DataField fld -> .... }`.

It's safer, because more explicit, to use the `DataField#getSubfield(char)` method from the marc4j API,
because expression evaluations return lists for the most part, and you won't know what you're operating on
if you use those expressions carelessly.  For those who need that specificity, the
extensions add a variation of this method (and for `DataField#getSubfields(char)`) that takes a `String` instead of a `char`
(the first character in the string is what will be used) so you can easily invoke them from Groovy code.

Operations that mutate (alter) records are somewhat more tricky, but the following will work:

- `record["245"][2] = '1'` - sets the value of the second indicator to  '1' (a `char`)
- `record["245"]["a"] = "2 Kill 2 Mockingbird"` - sets the value of subfield 'a'.
- `record["035"] << ["a|(OCoLC)ocm0001"]` adds a subfield 'a' with a value to an 035 field.

This last should be unpacked -- if the field already exists, the subfield will be added to that field.  If the tag
does not exist on the record, a new DataField will be created, and the subfield added.  Such "autovivified" fields will
 have both indicators set to blank.

The extensions will generally let you do whatever marc4j does with respect to creating "badly formed" records (repeating nonrepeatable fields and subfields,
particularly), although it has a few gestures in the direction of supporting a "strict" mode.

Removing fields currently requires "dropping back" to the marc4j API, e.g.:

```record['999'].each {
    it ->  // 'it' will be a DataField object
        if ( it['v'] =~ /WITHDRAWN/ ) { // and yet this regex-based test works on the subfield "directly"!
            record.removeVariableField(it)
        }
    }
```

Finally, and this is something added to support a specific data migration task, the extension adds a `toXML()` method to the
marc4j `Record` class.  This will yield up the record converted to a MARCXML string, but note that the `marc:record` element
will be *wrapped in a `marc:collection` element*.

Some Examples:

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

Currently there is no usage guide beyond this README, but if you look at the source code of the
tests under `src/test/groovy`, you'll see some sample use patterns.

### License

GNU GPL v3.  (c) 2015 North Carolina State University.

#### Future Expansion

These are possibly bad ideas, stray thoughts, pipe dreams, etc.

- add some method aliases for common fields such as pymarc's 'title', 'author', and 'isbn' attributes on records.
- make the `toXML()` extension for Record objects smarter
- come up with better ways of mutating records.
- add a Groovy Builder that yields up marc4j Record objects.