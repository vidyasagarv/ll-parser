## Recursive Descent LL parser

### Overview

Repository of work for CS571 Programming Languages at Binghamton University, Fall 2015.

In this project, we built a Recursive Descent Parser to parse any grammar using Java 8.


### Project Specification

A grammar consists of one or more rule-sets. A rule-set consists of a non-terminal symbol, followed by a colon :, followed by one-or-more right-hand sides separated by pipe | symbols. The rule-set is terminated by a semi-colon ;. A right-hand side is a sequence of zero-or-more grammar symbols. A grammar symbol is either a non-terminal or terminal symbol. A non-terminal is an identifier starting with a lower-case English alphabetic letter followed by 0-or-more alphanumerics or underscores. A terminal is an identifier starting with a upper-case English alphabetic letter followed by 0-or-more alphanumerics or underscores.
A grammar may contain comments which start with // and extend to the end of the line. Whitespace within a grammar serve only to separate adjacent words and are otherwise ignored.

Once run, it should report the following errors:
-The presence of syntax errors in the grammar (i.e., the grammar does not meet the above requirements).
-If there are multiple rule-set’s for the same non-terminal.
-If the grammar uses a non-terminal within a rule for which there is no rule-set.
-If the grammar defines a non-terminal by some rule-set but there is no use of that non-terminal within some rule (note that the start symbol which is the non-terminal defined by the first rule always has a use).

It is permissible for your program to exit after reporting the first error.
If there are no problems with the grammar, then the program should print a line on stdout containing the following space-separated statistics for the grammar in GrammarFile. nRuleSets nNonTerminals nTerminals
where
nRuleSets
is the total number of rules in the grammar.
nNonTerminals
is the total number of occurrences of non-terminals (not necessarily distinct) in the grammar.
nTerminals
is the total number of occurrences of terminals (not necessarily distinct) in the grammar. The output for the example grammar should be:
java -cp target/prj1.jar edu.binghamton.cs571.GrammarStats arith.gram
5 22 11

### Design

Used Java 8 for development and Apache Ant as a build tool.

To parse any grammar we need a recursive parsing approach so I built this recursive descent parser grammar to parse any grammar:

```
grammar : rule grammar | EOF ;

rule : NONTERMINAL COLON rhs ;

rhs : tokenRest rhs | SEMICOLON ;

tokenRest : NONTERMINAL | TERMINAL | PIPE ;

```

Now this grammar was converted to a Java program.

Scanner Class is built from a mapping of regex strings to some enum of token-kinds. If regex string is empty, then used for EOF token. If mapped enum is null, then input matching the corresponding regex is ignored.

Token Class is a simple struct representing a token produced by the scanner which has kind, lexeme, coords to identify any token.

GrammarStats is the main Class to call the parser function and display the grammar statistics.

1. In GrammarStats, all lhs and rhs non-terminals are stored in TreeSet to maintain uniqueness.
2. In getStats() method, we check for a non-terminal within a rule for which there is no rule-set and call doParse() recursive method.
3. In doParse() method, first, we check for any token other than non-terminal and throw error if present.
4. Later, we check for multiple rule-set's for the same non-terminal and store all non-terminals of RHS in a set at the same time.
5. We check for a non-terminal by some rule-set but no use of that non-terminal within some rule.
6. Then we call a recursive function RHS() to parse RHS tokens recursively.
7. RHS() function counts nRuleSets, nTerminals and nNonTerminals while parsing tokens if atleast one right-hand side is present.
7. Make a recursive call to doParse() until we find EOF of the grammar.

### Note

The work done here may only be used as reference material, not to be submitted as your own, with or without edits.

Copyright © 2017

