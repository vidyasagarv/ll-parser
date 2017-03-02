package edu.binghamton.cs571;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

//TODO: Add comment specifying grammar for a grammar

/**
 * Main Class to call the parser function and display the grammar statistics
 * 
 * -----THE RECURSIVE DESCENT GRAMMAR LOOKS LIKE THE FOLLOWING-----
 * 
 * grammar : rule grammar | EOF ;
 * 
 * rule : NONTERMINAL COLON rhs ;
 * 
 * rhs : tokenRest rhs | SEMICOLON ;
 * 
 * tokenRest : NONTERMINAL | TERMINAL | PIPE ;
 * 
 */

public class GrammarStats {

	private final Scanner _scanner;
	private Token _lookahead;
	// TODO: add other fields as necessary

	/** fields to represent the counters */
	public int nRule, nTerminal, nNonterminal, nRHS = 0;

	/** all lhs non-terminals are stored in TreeSet to maintain uniqueness */
	public Set<String> lhsNTs = new TreeSet<String>();
	/** all rhs non-terminals are stored in TreeSet to maintain uniqueness */
	public Set<String> rhsNTs = new TreeSet<String>();

	GrammarStats(String fileName) {
		_scanner = new Scanner(fileName, PATTERNS_MAP);
		nextToken();
	}

	/**
	 * Recognize a grammar specification. Return silently if ok, else signal an
	 * error.
	 */
	Stats getStats() {
		Stats stats = null;
		try {
			// TODO: Call top-level parsing function, throw semantic errors,

			// create stats struct if everything ok.

			Boolean b = doParse();

			if (b) {
				// check for a non-terminal within a rule for which there is no
				// rule-set
				rhsNTs.removeAll(lhsNTs);
				if (rhsNTs.size() > 0) {
					System.err
							.println("No rule set is associated with the tokens..");
					rhsNTs.forEach(System.err::println);
					b = false;
				} else {
					stats = new Stats(nRule, nTerminal, nNonterminal);
				}
			}
		} catch (GrammarParseException e) {
			System.err.println(e.getMessage());
		}
		return stats;
	}

	/**
	 * 
	 * main method to initialize parsing and match tokens parses each rule set
	 * recursively uses return value to indicate errors in the grammar
	 * 
	 * @return boolean
	 */
	public boolean doParse() {
		if (_lookahead.kind == TokenKind.EOF) {

			// empty input file
			if (nRule == 0) {
				// throw error
				if (_lookahead.kind == TokenKind.EOF) {
					throw new GrammarParseException(
							" Empty input grammar file ");
				}
			}

			// end of grammar file
			match(TokenKind.EOF);
		} else {
			try {
				// check for any token other than non-terminal
				if (_lookahead.kind != TokenKind.NON_TERMINAL) {
					// throw error
					syntaxError();
				} else {

					// check for multiple rule-set's for the same non-terminal
					// store all non-terminals of RHS in a set at the same time
					if (!lhsNTs.add(_lookahead.lexeme)) {
						// throw error
						syntaxError();
					}
					nNonterminal++;
					if (nNonterminal != 1) {
						// check for a non-terminal by some rule-set but no use
						// of that non-terminal within some rule
						if (rhsNTs.add(_lookahead.lexeme)) {
							// throw error
							syntaxError();
						}
					}
					match(TokenKind.NON_TERMINAL);
					if (_lookahead.kind == TokenKind.COLON) {
						match(TokenKind.COLON);
					} else {
						// throw error
						syntaxError();
					}
					// call RHS() function to parse RHS tokens recursively
					RHS();

					// recursive call
					return doParse();
				}

			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
				return false;
			}
		}
		return true;

	}

	/**
	 * 
	 * parses RHS of a rule set recursively
	 * 
	 * @return void
	 */
	public void RHS() {

		if (_lookahead.kind == TokenKind.SEMI) {
			// check for atleast one right-hand side
			if (nRHS == 0) {
				syntaxError();
			}
			match(TokenKind.SEMI);
			nRule++;
		} else {
			if (_lookahead.kind == TokenKind.TERMINAL) {
				match(TokenKind.TERMINAL);
				nTerminal++;
			} else if (_lookahead.kind == TokenKind.NON_TERMINAL) {
				// store all non-terminals of RHS in a set
				rhsNTs.add(_lookahead.lexeme);
				match(TokenKind.NON_TERMINAL);
				nNonterminal++;
			} else if (_lookahead.kind == TokenKind.PIPE) {
				match(TokenKind.PIPE);

			} else {
				syntaxError();
			}
			// recursive call
			nRHS++;
			RHS();
		}

	}

	// We extend RuntimeException since Java's checked exceptions are
	// very cumbersome
	private static class GrammarParseException extends RuntimeException {
		GrammarParseException(String message) {
			super(message);
		}
	}

	private void match(TokenKind kind) {
		if (kind != _lookahead.kind) {
			syntaxError();
		}
		if (kind != TokenKind.EOF) {
			nextToken();
		}
	}

	/** Skip to end of current line and then throw exception */
	private void syntaxError() {
		String message = String.format("%s: syntax error at '%s'",
				_lookahead.coords, _lookahead.lexeme);
		while (_lookahead.kind != TokenKind.EOF) {
			nextToken();
		}
		throw new GrammarParseException(message);
	}

	private static final boolean DO_TOKEN_TRACE = false;

	private void nextToken() {
		_lookahead = _scanner.nextToken();
		if (DO_TOKEN_TRACE)
			System.err.println("token: " + _lookahead);
	}

	/** token kinds for grammar tokens */
	private static enum TokenKind {
		EOF, COLON, PIPE, SEMI, NON_TERMINAL, TERMINAL, ERROR
	}

	/** Simple structure to collect grammar statistics */
	private static class Stats {
		final int nRuleSets;
		final int nNonTerminals;
		final int nTerminals;

		Stats(int nRuleSets, int nNonTerminals, int nTerminals) {
			this.nRuleSets = nRuleSets;
			this.nNonTerminals = nNonTerminals;
			this.nTerminals = nTerminals;
		}

		public String toString() {
			return String.format("%d %d %d", nRuleSets, nNonTerminals,
					nTerminals);
		}
	}

	/** Map from regex to token-kind */
	private static final LinkedHashMap<String, Enum> PATTERNS_MAP = new LinkedHashMap<String, Enum>() {
		{
			put("", TokenKind.EOF);
			put("\\s+", null); // ignore whitespace.
			put("\\//.*", null); // ignore // comments
			put("\\:", TokenKind.COLON);
			put("\\|", TokenKind.PIPE);
			put("\\;", TokenKind.SEMI);
			put("[a-z]\\w*", TokenKind.NON_TERMINAL);
			put("[A-Z]\\w*", TokenKind.TERMINAL);
			put(".", TokenKind.ERROR); // catch lexical error in parser
		}
	};

	private static final String USAGE = String.format(
			"usage: java %s GRAMMAR_FILE", GrammarStats.class.getName());

	/**
	 * Main program for testing
	 * 
	 * @throws ReflectiveOperationException
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(USAGE);
			System.exit(1);
		}

		GrammarStats grammarStats = new GrammarStats(args[0]);
		Stats stats = grammarStats.getStats();
		if (stats != null) {
			System.out.println(stats);
		}
	}

}
