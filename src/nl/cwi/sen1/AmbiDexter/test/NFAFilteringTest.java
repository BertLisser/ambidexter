package nl.cwi.sen1.AmbiDexter.test;

import java.util.regex.Pattern;

import nl.cwi.sen1.AmbiDexter.Main;
import nl.cwi.sen1.AmbiDexter.grammar.Symbol;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;
import nl.cwi.sen1.AmbiDexter.util.Pair;
import nl.cwi.sen1.AmbiDexter.util.Relation;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NFAFilteringTest extends TestCase {

	public static Object[][] diff1; // what sglr finds and we don't
	public static Object[][] diff2; // what we find and sglr doesn't
	
	public static Test suite(){
		return new TestSuite(NFAFilteringTest.class);
	}
	
	public void testDerivGenScannerless() {
		
		testScannerlessGrammar("sdf/C.norm.impl.pt", 4, "sdf/C-All.tbl", new Object[][] {{},{},{},{"int[a-z]"}, {}},
				new Object[][] {{},{},{},{"int[a-z]"},{"*inty", "voido", "inty", Pattern.compile("inty[a-z]")}}		
		); // slgr 5 takes 11hrs 40min on laptop */
		
		testScannerlessGrammar("sdf/ECMAScript.norm.impl.pt", 4, "sdf/ECMAScript-All.tbl", new Object[][] {{},{},{},{"\\[\t\\$\\]", "\\[\n\\$\\]", "\\[\r\\$\\]", "\\{\t\\$\\}", "\\{\n\\$\\}", "\\{\r\\$\\}", Pattern.compile("\\$in[a-z01_]"),
			"var_" // parsed, but SGLR finds only non-optimal parsetree, so not recognized for given symbol
			}},
				new Object[][] {{},{},{},{}}
		); // maybe 5 is possible as well */
		
		testScannerlessGrammar("sdf/sql92.norm.impl.pt", 5, "sdf/sql92-All.tbl", new Object[][]{{},{},{},{Pattern.compile("AS[A-Z]")},{
			Pattern.compile("AS[A-Z]"),
			Pattern.compile("AS[A-Z][A-Z\t]"),
			Pattern.compile("[STV]\tAS[A-Z]"), // ambiguous b/c of AS
		},{},{}},
				new Object[][]{{},{},{},{},{},{},{}}
		); // maybe 6 is possible as well */
		
		testScannerlessGrammar("sdf/java.norm.impl.pt", 10, "sdf/java-All.tbl", new Object[][]{{},{},{},{},{},{},{},{},{},{},{Pattern.compile("[a-z]=[a-z01]")},{},{}},
				//the java ambiguity a=b is found by ambidexter at length 13, b/c of priorities
				new Object[][]{{},{},{},{},{},{},{},{},{},{},{},{},{}}
		); // 12 is possible as well, would take 2hrs */
				
		testScannerlessGrammar("sdf/Java-15.norm.impl.pt", 5, "sdf/Java-15-All.tbl", new Object[][]{{},{},{},{},{},{}},
				new Object[][]{{},{},{},{},{},{}} // first ambiguity of AmbiDexter at length 7, cf(Expr): '0' '0' '.' 'D'
		); // maybe 7 is also possible, would take around 3hrs */
		
		testScannerlessGrammar("sdf/Cxx.norm.impl.pt", 2, "sdf/Cxx-All.tbl", new Object[][]{{},{Pattern.compile("[hpxy]"), Pattern.compile("[abcdefilmnorstuvw][a-z]"),
			Pattern.compile("\t[a-z]")}},
				// TODO 2
				// cf(DeclSpecifier+): 'a' 'a'
				// cf(SimpleTypeSpecifier): '       ' 'a'
				new Object[][]{{},{}}
		); //*/

		testScannerlessGrammar("sdf/Stratego.norm.impl.pt", 10, "sdf/Stratego-All.tbl", "sdf/Stratego.tbl");
		// unambiguous up to now.... */
	}
	
	private void testScannerlessGrammar(String grammar, int len, String parseTable, String prefixParseTable) {
		testScannerlessGrammar(grammar, len, parseTable, null, null);
	}

	private void testScannerlessGrammar(String grammar, int len, String parseTable, Object[][] diff1, Object[][] diff2) {
		ScannerlessDerivGenTest.diff1 = diff1;
		ScannerlessDerivGenTest.diff2 = diff2;
		Main m = new Main();
		m.executeArguments((new String[] {"-q", "-pgp", "-k", String.valueOf(len), "-ogs", "-rdfa", "unf", grammar}));
	}
	
	public static void compareAmbiguities(Relation<Symbol, SymbolString> sglr, Relation<Symbol, SymbolString> scan, int depth) {
		boolean different = false;
		different |= checkDifference(sglr, scan, diff1 == null ? null : diff1[depth - 1], "Ambiguous by SGLR: ");
		different |= checkDifference(scan, sglr, diff2 == null ? null : diff2[depth - 1], "Ambiguous by AmbiDexter: ");
		assertFalse(different);
	}

	private static boolean checkDifference(Relation<Symbol, SymbolString> r1, Relation<Symbol, SymbolString> r2, Object[] diff, String message) {
		boolean different = false;
		for (Pair<Symbol, SymbolString> p : r1) {
			if (!r2.contains(p)) {
				boolean found = false;
				if (diff != null) {
					String a = p.b.toAscii();
					for (int i = diff.length - 1; i >= 0 && !found; --i) {
						if (diff[i] instanceof Pattern) {
							if (((Pattern) diff[i]).matcher(a).find()) { // TODO multiple matches might be possible
								found = true;
								break;
							}							
						} else if (diff[i].equals(a)) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					System.out.println(message + p.a + ", " + p.b.prettyPrint());
					different = true;
				}
			}
		}
		return different;
	}
}

