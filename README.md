# Groovy MARC Extensions

This project contains a set of Groovy Extensions and utilities for [marc4j](https://github.com/marc4j/marc4j)
that allow for more "groovyesque" processing of MARC records.

## Background

MARC records don't really support a path-like expression language for navigation
and manipulation, but they get close enough that I thought I'd give it a shot; part of
the inspiration here is [pymarc](https://github.com/edsu/pymarc).

I'm not going to try to sell you on Groovy, other than to note that you might
want to run MARC processing scripts and let's assume that the JVM or marc4j's strengths make it seem like a good idea
to you, but you're not sure you want to write all your processing logic in Java.

Groovy suggests itself as an option in this case.

The problem with just importing marc4j and proceeding in Groovy is that, *despite* its
general strength in Java integration, Groovy makes dealing with `char`
literals (which are all over the place in the marc4j API) a bit painful.  Moreover, going from fluid
dynamic groovy code style to Java code style can be a bit jarring.

So this project tries to sweep most of that under the rug.  It also tries to make creating
marc4j API objects pretty simple, e.g. `'$athis is my subfield a'` can, in the right contexts,
be turned into an appropriate `Subfield` object without having to (explicitly) fiddle with
marc4j's `MarcFactory` class.  You can also use Groovy syntax such as the `leftShift` (`<<`) operator
to quickly add bunches of fields or subfields.

Finally, it adds a factory class `edu.ncsu.lib.marc.ReaderFactory` that can produce decorated marc4j `MarcReader`
instances, allowing "groovyesque" methods of reading, modifying, and collecting MARC records (e.g.

    // automatically detect you're reading a MARC21 file with platform-default encoding.
    def reader = ReaderFactory.newReader( new File("mymarc.mrc") )
    reader.each { Record rec -> ... }


If you compile this library using JDK 8, you can use the Streams API with the reader returned by the factory,
including auto-parallelizing processing, Java 8 lambdas, and things like that.  When you use JDK 7, the reader
implements the older Iterable interface without streams (and the name of the artifact generated will include the "jdk7"
classifier in this case).

### Examples

In general, the idea is to allow access to the fields and subfields of a MARC
record using standard groovy map-style expressions, e.g.

- `record["245"]` - returns the 245 field on the record
- `record["035"]` - returns all the 035s on the record as a list.
- `record['245']['a']` - returns the 245$a subfield (as a list)
- `record['245'][1]` - gets the value of the first indicator (note integer literal here in second index).
- `record["245|a"]` - returns the 245 subfield 'a' values; this is equivalent to the second example above.

Note that '|' and '$' are both valid subfield delimiters in an expression, but '$' in a *double-quoted* Groovy string
indicates interpolation (that is: "245$a" means roughly 'the literal 245 with the Stringified value of the variable
'a'"), so you will either have to use single quoted strings or escape the '$' to use the more common form, e.g. the
above expression is equivalent to both `record['245$a']` and `record["245\$a"]`.

The general default for these expressions is to return lists, because the default assumption of the framework is that
a field is repeatable.  The 245 has been explicitly "tagged" by the framework as non-repeatable, however,
and so expressions involving it return the single value.  The tagging work is not yet complete, however, so *most*
expressions will return lists of fields.

If you want to look at all the 035s,(e.g.) you would do something like `record["035"].each {
    DataField fld -> .... }`.

A feature/quirk of the extensions is that if you *assign* to a repeatable field, you are *deleting* all of the
existing fields with the same tag.  This should be unsurprising given the semantics of assignment, but the quirk is that
you can also assign a single subfield value and this has the same semantics as assignment to a list with a single value,
`record['035'] = '|a(WaSSeSS)ssj12345'` as opposed to `record['035'] << [ "|a(WaSSeSS)ssj12345" ]`.  I may be unclear on
how best to apply the principle of Least Surprise in this feature.

It's safer, because more explicit, to use the `DataField#getSubfield(char)` method from the marc4j API,
because expression evaluations return lists for the most part, and you won't know what you're operating on
if you use those expressions carelessly.  For those who need that specificity, the
extensions add a variation of this method (and for `DataField#getSubfields(char)`) that takes a `String` instead of a `char`
(the first character in the string is what will be used) so you can easily invoke them from Groovy code.

In order to streamline testing for field and subfield existence a bit, when using the expressions to denote
 a non-existent field, the extensions return a `NullDataField` that

- evaluates to `false` in standard Groovy boolean contexts
- prevents (some) null pointer exceptions when accessing subfields, so that e.g. `record['988|a'] == "whatever"` is a compact
 way to check whether the record has a 988$a with the appropriate value.
- allows autovivification of the field if you decide you want to assign to it.  So, if I just want to make sure that
 a record has an 035 with an appropriate value, for example, I can use the expression `record['035'] << 'a|(OCoLC)ocm0001'`

In general, operations involving expressions that mutate (alter) records are somewhat more tricky, but the following will work:

- `record["245"][2] = '1'` - sets the value of the second indicator to  '1' (a `char`)
- `record["245"]["a"] = "2 Kill 2 Mockingbird"` - sets the value of subfield 'a'.

The extensions will generally let you do whatever marc4j does with respect to creating "badly formed" records (repeating nonrepeatable fields and subfields,
particularly), although it has a few gestures in the direction of supporting a "strict" mode.

Removing fields currently requires "dropping back" to the marc4j API, e.g.:

    record['999'].each {
        it ->  // 'it' will be a DataField object
            if ( it['v'] =~ /WITHDRAWN/ ) { // and yet this regex-based test works on the subfield "directly"!
                record.removeVariableField(it)
            }
    }

Finally, and this is something added to support a specific data migration task, the extension adds a `toXML()` method to the
marc4j `Record` class.  This will yield up the record converted to a MARCXML string, but note that the `marc:record` element
will be *wrapped in a `marc:collection` element*.

### Some Examples:

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
            // in this API we return a string because it's specifically for use in Groovy
             println "Non-International ISSN: ${issn['a']}"
        }
    }

### Usage

Build the extensions with [Gradle](http://www.gradle.org), and put the
resulting jar (output into `build/libs/groovy-marc-${VERSION}.jar`) on your classpath.

Groovy loads and makes the extensions available for use if the JAR is on your classpath,  
without further intervention on your part.

Currently there is no usage guide beyond this README, but if you look at the source code of the
tests under `src/test/groovy`, you'll see some sample use patterns.

### License

GNU GPL v3.  (c) 2015 North Carolina State University.

#### Future Expansion

These are possibly bad ideas, stray thoughts, pipe dreams, etc.

- add some aliases for commonly-used fields such as pymarc's 'title', 'author', and 'isbn' attributes on records.
- while we're there, add some more aliases for commonly-used fixed fields (leader/008).
- make the `toXML()` extension for Record objects smarter
- come up with better ways of mutating records.
- add a Groovy Builder that yields up marc4j Record objects.
- make repeatable/non-repeatable field "enforcement" optional, i.e. add a "strict mode" that is complete.
- some kind of declarative validation DSL/framework
