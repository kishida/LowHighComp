/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import org.codehaus.jparsec.error.ParserException;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static kis.lowhighcomp.LowHighComp.*;

/**
 *
 * @author naoki
 */
public class LowHighCompTest{
    

    @Test
    public void whitespaceTest成功() {
        LowHighComp.whitespace().parse("   ");
        LowHighComp.whitespace().parse(" \t  ");
        LowHighComp.whitespace().parse("");
    }
    @Test(expected = ParserException.class)
    public void whitespaceTest失敗() {
        LowHighComp.whitespace().parse("   aa");
    }
    @Test
    public void 改行(){
        LowHighComp.newline().parse("\r\n");
        LowHighComp.newline().parse("\n");
        LowHighComp.newline().parse("\n\n");
    }
    @Test(expected = ParserException.class)
    public void 改行失敗(){
        LowHighComp.newline().parse("\n  ");
    }
    
    @Test
    public void 整数(){
        assertThat(LowHighComp.integer().parse("123"), is(new IntegerExp(123)));
        assertThat(LowHighComp.integer().parse("1200"), is(new IntegerExp(1200)));
    }
    
    @Test
    public void 識別子(){
        assertThat(LowHighComp.identifier().parse("a"), is(new VariableExp("a")));
        assertThat(LowHighComp.identifier().parse("a123"), is(new VariableExp("a123")));
        assertThat(LowHighComp.identifier().parse("abbbz"), is(new VariableExp("abbbz")));
        assertThat(LowHighComp.identifier().parse("a1zz"), is(new VariableExp("a1zz")));
    }
    @Test
    public void 配列(){
        System.out.println(LowHighComp.array().parse("hoge[123]"));
        System.out.println(LowHighComp.array().parse("hoge[aaa]"));
        System.out.println(LowHighComp.array().parse("hoge"));
    }
    
    @Test
    public void 値(){
        System.out.println(LowHighComp.value().parse("hoge[123]"));
        System.out.println(LowHighComp.value().parse("hoge[aaa]"));
        System.out.println(LowHighComp.value().parse("hoge"));
        System.out.println(LowHighComp.value().parse("123"));
    }
    
    @Test
    public void 宣言(){
        System.out.println(LowHighComp.define().parse("var hoge[123]"));
        System.out.println(LowHighComp.define().parse("var hoge"));
        System.out.println(LowHighComp.define().parse("var hoge[aaa]"));
    }
    @Test
    public void 演算２項(){
        System.out.println(LowHighComp.expression().parse("1 * 2"));
        System.out.println(LowHighComp.expression().parse("1"));
        System.out.println(LowHighComp.expression().parse("a + b"));
        System.out.println(LowHighComp.expression().parse("a[12] + b[u]"));
        System.out.println(LowHighComp.expression().parse("a"));
        System.out.println(LowHighComp.expression().parse("a[ 4]"));
    }
    @Test
    public void 割り当て(){
        System.out.println(assignment().parse("a = 12"));
        System.out.println(assignment().parse("a[i] = 12 * 3"));
        System.out.println(assignment().parse("a[12] = 12 + a[b]"));
        System.out.println(assignment().parse("a = a[i]"));
    }
    @Test
    public void ループ(){
        System.out.println(loop().parse("loop 12 for i\na = i\nvar n\nn=12 * i\nend"));
    }
    @Test
    public void 引数(){
        System.out.println(argument().parse(" in test"));
        System.out.println(argument().parse(" out test[8]"));
        System.out.println(argument().parse(" inout test[8]"));
        System.out.println(argument().parse(" inout out"));
    }
    
    @Test
    public void モジュール(){
        String script =
                "module shuffle(inout data[8])\n"
                + "  loop 8 for i\n"
                + "    data[i] = i + 1\n"
                + "  end\n"
                + "end\n";
        System.out.println(module().parse(script));
        String script2 =
                " module shuffle(inout data[8])\n"
                + "  loop 8 for i\n"
                + "    data[i] = i + 1\n"
                + "  end\n"
                + "  var r\n"
                + "  r = 12344\n"
                + "  loop 100 for i\n"
                + "    r = r * 211\n"
                + "    var p1\n"
                + "    p1 = r / 8\n"
                + "    data[p1] = r\n"
                + "  end\n"
                + " end";
        System.out.println(module().parse(script2));
    }
}
