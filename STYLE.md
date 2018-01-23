# Styleguide

*Line breaks*

There are generally two reasons to insert a line break:
1. Your statement exceeds the column limit.
1. You want to logically separate a thought.
Writing code is like telling a story. Written language constructs like chapters, paragraphs, and punctuation (e.g. semicolons, commas, periods, hyphens) convey thought hierarchy and separation. We have similar constructs in programming languages; you should use them to your advantage to effectively tell the story to those reading the code.

*Indent style*

We use the "one true brace style" (1TBS). Indent size is a tab.

*100 column limit*

You should follow the convention set by the body of code you are working with. We tend to use 100 columns for a balance between fewer continuation lines but still easily fitting two editor tabs side-by-side on a reasonably-high resolution display.

*Space pad operators and equals.*

*Naming variables*

CamelCase for types, camelCase for variables, UPPER_SNAKE for constants

*Documenting a method*

A method doc should tell what the method does. Depending on the argument types, it may also be important to document input format.

Don't document overriding methods (usually)

*Import ordering*

Imports are grouped by top-level package, with blank lines separating groups. Static imports are grouped in the same way, in a section below traditional imports.

*Avoid typecasting*

Typecasting is a sign of poor class design, and can often be avoided. An obvious exception here is overriding equals.
