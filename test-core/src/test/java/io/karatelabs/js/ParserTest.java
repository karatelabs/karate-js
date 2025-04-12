package io.karatelabs.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {

    private static void equals(String text, String json, Type type) {
        Parser parser = new Parser(Source.of(text));
        Node node = parser.parse();
        Node child;
        if (type == null) {
            child = node;
        } else {
            Node found = node.findFirst(type);
            child = found.children.get(0);
        }
        NodeUtils.assertEquals(text, child, json);
    }

    private static Chunk firstNumber(String text) {
        Parser parser = new Parser(Source.of(text));
        Node root = parser.parse();
        Node num = root.findFirst(Token.NUMBER);
        return num.chunk;
    }

    private static void expr(String text, String json) {
        equals(text, json, Type.STATEMENT);
    }

    private static <T> void error(String text, Class<T> type) {
        try {
            Parser parser = new Parser(Source.of(text));
            parser.parse();
            fail("expected exception of type: " + type);
        } catch (Exception e) {
            if (!e.getClass().equals(type)) {
                fail("expected exception of type: " + type + ", but was: " + e.getClass());
            }
        }
    }

    private static void program(String text, String json) {
        equals(text, json, null);
    }

    @Test
    void testDev() {

    }

    @Test
    void testBlock() {
        expr("{ a }", "['{',$a,'}']");
        expr("{ 1; 2 }", "['{',[1,';'],2,'}']");
    }

    @Test
    void testProgram() {
        program("1;2", "{PROGRAM:[[1,';'],2]}");
        program("1\n2", "{PROGRAM:[1,2]}");
        program("1 \n 2", "{PROGRAM:[1,2]}");
        program("1 \n 2 ", "{PROGRAM:[1,2]}");
        program("1;2;", "{PROGRAM:[[1,';'],[2,';']]}");
        program("1 ;2", "{PROGRAM:[[1,';'],2]}");
        program("1;2 ", "{PROGRAM:[[1,';'],2]}");
        program("1;2; ", "{PROGRAM:[[1,';'],[2,';']]}");
    }

    @Test
    void testAddExpr() {
        expr("1 + 2", "[1,'+',2]");
        expr("2 - 1", "[2,'-',1]");
        expr("1 + 2 + 3", "[[1,'+',2],'+',3]");
        expr("1 - 2 + 3", "[[1,'-',2],'+',3]");
    }

    @Test
    void testMulExpr() {
        expr("2 * 3", "[2,'*',3]");
        expr("6 / 2", "[6,'/',2]");
    }

    @Test
    void testAddMul() {
        expr("1 * 2 + 3", "[[1,'*',2],'+',3]");
        expr("1 + 2 * 3", "[1,'+',[2,'*',3]]");
    }

    @Test
    void testExp() {
        expr("2 ** 3", "[2, '**', 3]");
        expr("1 ** 2 ** 3", "[1,'**',[2,'**',3]]");
        expr("(2 ** 3) ** 2", "[[2,'**',3],'**',2]");
    }

    @Test
    void testPostExpr() {
        expr("a++", "[$a,'++']");
        expr("b--", "[$b,'--']");
        expr("a = b++", "[$a,'=',[$b,'++']]");
    }

    @Test
    void testPreExpr() {
        expr("++a", "['++',$a]");
        expr("--b", "['--',$b]");
        expr("a = --b", "[$a,'=',['--',$b]]");
    }

    @Test
    void testBitwise() {
        expr("1 | 2", "[1,'|',2]");
        expr("5 | 1 | 2", "[[5,'|',1],'|',2]");
    }

    @Test
    void testPrimitives() {
        expr("1", "1");
        expr("null", "null");
    }

    @Test
    void testParen() {
        expr("(1)", "1");
        expr("(1 + 3) * 2", "[[1,'+',3],'*',2]]");
        expr("2 * (1 + 3)", "[2,'*',[1,'+',3]]");
    }

    @Test
    void testStrings() {
        expr("'foo'", "foo");
        expr("\"foo\"", "foo");
        expr("\"\\\"foo\\\"\"", "\\\"foo\\\"");
        expr("'\\'foo\\''", "\\'foo\\'");
        expr("read('fooRbar')", "[$read,'(','fooRbar',')']");
    }

    @Test
    void testRegex() {
        expr("/foo/", "/foo/");
        expr("(/a\\/b/)", "/a\\/b/");
        expr("/foo/i", "/foo/i");
        expr("var re1 = /test/", "[var,$re1,'=','/test/']");
    }

    @Test
    void testPathExpr() {
        expr("a", "$a");
        expr("a.b", "[$a,'.',$b]");
        expr("a.b.c", "[[$a,'.',$b],'.',$c]");
        expr("a.b.c.d", "[[[$a,'.',$b],'.',$c],'.',$d]");
        expr("a.b[c]", "[[$a,'.',$b],'[',$c,']']");
        expr("a.b[c].d", "[[[$a,'.',$b],'[',$c,']'],'.',$d]");
        expr("a[b].c[d]", "[[[$a,'[',$b,']'],'.',$c],'[',$d,']']");
        expr("a[b].c", "[[$a,'[',$b,']'],'.',$c]");
        expr("a['b']", "[$a,'[',b,']']");
        expr("a[b]", "[$a,'[',$b,']']");
        expr("a['b']['c']", "[[$a,'[',b,']'],'[',c,']']");
        expr("a[b][c]", "[[$a,'[',$b,']'],'[',$c,']']");
    }

    @Test
    void testPathExprReservedWords() {
        expr("a.null", "[$a,'.',null]");
    }

    @Test
    void testPathMix() {
        expr("(a)", "$a");
        expr("(a).b", "[$a,'.',$b]");
        expr("a[(b)]", "[$a,'[',$b,']']");
        expr("a[b + 'c']", "[$a,'[',[$b,'+',c],']']");
    }

    @Test
    void testObject() {
        expr("{}", "['{','}']");
        expr("{ a: 1 }", "['{',[$a:,1],'}']");
        expr("{ a: 'b' }", "['{',[$a:,b],'}']");
    }

    @Test
    void testArray() {
        expr("[]", "['[',']']");
        expr("[1]", "['[',1,']']");
        expr("[1,]", "['[',1,']']");
        expr("[a]", "['[',$a,']']");
        expr("['a']", "['[',a,']']");
        expr("[1,2]", "['[',1,2,']']");
        expr("[1,2,3]", "['[',1,2,3,']']");
    }

    @Test
    void testFnExpr() {
        expr("function(){}", "[function,'(',[],')',['{','}']]");
        expr("function(){ return true }", "[function,'(',[],')',['{',['return',true],'}']]");
        expr("function(a){ return a }", "[function,'(',$a,')',['{',['return',$a],'}']]");
        expr("function(a){ return { a } }", "[function,'(',$a,')',['{',['return',['{',$a,'}']],'}']]");
        expr("function(a){ return { a, b } }", "[function,'(',$a,')',['{',['return',['{',$a,$b,'}']],'}']]");
    }

    @Test
    void testFnCall() {
        expr("a.b()", "[[$a,'.',$b],'(',[],')']");
        expr("foo()", "[$foo,'(',[],')']");
        expr("foo.bar()", "[[$foo,'.','$bar'],'(',[],')']");
    }

    @Test
    void testFnExprArrow() {
        expr("() => true", "['(',[],')','=>',true]");
        expr("() => {}", "['(',[],')','=>',['{','}']]");
        expr("a => true", "[$a,'=>',true]");
        expr("(a) => true", "['(',$a,')','=>',true]");
        expr("(a, b) => true", "['(',[[$a,','],$b],')','=>',true]");
        expr("a => { return true }", "[$a,'=>',['{',['return',true],'}']]");
    }

    @Test
    void testVarStatement() {
        expr("var foo", "[var,$foo]");
        expr("var foo, bar", "[var,[$foo,','$bar]]");
        expr("var foo = 1", "[var,$foo,'=',1]");
        expr("var foo, bar = 1", "[var,[$foo,','$bar],'=',1]");
        expr("var a, b = 1 + 2", "[var,[$a,','$b],'=',[1,'+',2]]");
    }

    @Test
    void testAssignStatement() {
        expr("a = 1", "[$a,'=',1]");
        expr("a.b = 1", "[[$a,'.',$b],'=',1]");
        expr("a.b.c = 1", "[[[$a,'.',$b],'.',$c],'=',1]");
        expr("a = 1 + 2", "[$a,'=',[1,'+',2]]");
        expr("a = 2 * 3", "[$a,'=',[2,'*',3]]");
        expr("a = function(){ return true }", "[$a,'=',[function,'(',[],')',['{',['return',true],'}']]]");
    }

    @Test
    void testCommaExpression() {
        expr("a, b, c", "[$a,',',$b,','$c]");
    }

    @Test
    void testAssignBitShift() {
        expr("n >>>= 0", "[$n,'>>>=',0]");
        expr("n >>= 0", "[$n,'>>=',0]");
    }

    @Test
    void testIfStatement() {
        expr("if (true) a = 1", "['if','(',true,')',[$a,'=',1]]");
        expr("if (true) a = 1; else a = 2", "['if','(',true,')',[[$a,'=',1]';'],'else',[$a,'=',2]]");
    }

    @Test
    void testForStatement() {
        expr("for(;;){}", "['for','(',';',';',')',['{','}']]");
    }

    @Test
    void testTernary() {
        expr("true ? 'foo' : bar", "[true,'?','foo',':',$bar]");
    }

    @Test
    void testLogicalExpr() {
        expr("a < b", "[$a,'<',$b]");
        expr("x = a >= b", "[$x,'=',[$a,'>=',$b]]");
    }

    @Test
    void testTryStmt() {
        expr("try {} catch (e) {}", "['try',['{','}'],'catch','(',$e,')',['{','}']]");
        expr("try {} finally {}", "['try',['{','}'],'finally',['{','}']]");
        expr("try {} catch (e) {} finally {}", "['try',['{','}'],'catch','(',$e,')',['{','}'],'finally',['{','}']]");
        expr("try {} catch {}", "['try',['{','}'],'catch',['{','}']]");
    }

    @Test
    void testTypeOf() {
        expr("typeof 'foo' === 'string'", "[['typeof','foo'],'===','string']");
    }

    @Test
    void testInstanceOf() {
        expr("foo instanceof Foo", "[$foo, 'instanceof', $Foo]");
    }

    @Test
    void testSyntaxError() {
        error("function", ParserException.class);
    }

    @Test
    void testTemplate() {
        expr("``", "['`','`']");
        expr("`foo`", "['`','foo','`']");
        expr("`${}`", "['`','${', '}','`']");
        expr("`${foo}`", "['`','${', '$foo', '}','`']");
        expr("`${1 + 2}`", "['`','${',[1,'+',2],'}','`']");
        expr("`[${}]`", "['`','[','${','}',']','`']");
    }

    @Test
    void testWhiteSpaceCounting() {
        Chunk chunk = firstNumber("/* */  1");
        assertEquals(0, chunk.line);
        assertEquals(7, chunk.col);
        assertEquals(7, chunk.pos);
        chunk = firstNumber("/* \n* \n*/\n 1");
        assertEquals(3, chunk.line);
        assertEquals(1, chunk.col);
        assertEquals(11, chunk.pos);
        chunk = firstNumber("// foo \n // bar \n1");
        assertEquals(2, chunk.line);
        assertEquals(0, chunk.col);
        chunk = firstNumber("\n  \n  1");
        assertEquals(2, chunk.line);
        assertEquals(2, chunk.col);
        assertEquals(6, chunk.pos);
    }

    @Test
    void testBacktickEdgeCases() {
        error("`", ParserException.class);
    }

    @Test
    void testRegexEofEdgeCases() {
        error("<x>x</", IndexOutOfBoundsException.class);
        error("<foo>foo</foo>\n", IndexOutOfBoundsException.class);
    }

}
