/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naoki
 */
public class CompScriptTest {
    
    public CompScriptTest() {
    }

    @Test
    public void testSomeMethod() {
        String script = ""+ 
            "    module shuffle(out data[8])\n" +
            "      loop 8 for i\n" +
            "        data[i] = i + 1\n" +
            "      end\n" +
            "      var r\n"+
            "      var p1\n" +
            "      var p2\n" +
            "      var tmp\n" +
            "      r = 2525\n" +
            "      loop 200 for i\n" +
            "        r = r * 211\n" +
            "        r = r + 2111\n" +
            "        r = r % 1999\n" +
            "        p1 = r / 8\n" +
            "        p1 = r % 8\n" +
            "        r = r * 211\n" +
            "        r = r + 2111\n" +
            "        r = r % 1999\n" +
            "        p2 = r / 8\n" +
            "        p2 = r % 8\n" +
            "        tmp = data[p1]\n" +
            "        data[p1] = data[p2]\n" +
            "        data[p2] = tmp\n" +
            "      end\n" +
            "   end";
        Map<String, Object> result = CompScript.eval(script, new int[8]);
        System.out.println(Arrays.toString((int[])result.get("data")));
    }
    
}
