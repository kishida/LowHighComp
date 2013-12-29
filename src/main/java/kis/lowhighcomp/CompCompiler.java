/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kis.lowhighcomp.LowHighComp.*;
import static kis.lowhighcomp.ObjectPatternMatch.*;

/**
 *
 * @author naoki
 */
public class CompCompiler {
    public static String defvar(ValueExp exp){
        return matchRet(exp,
            caseOfRet(ArrayExp.class,  arr ->{
                return String.format("[15:0] %s[%s]", 
                        arr.variable.name, evalvar(arr.index));
            }),
            caseOfRet(VariableExp.class, var ->{
                return String.format("[15:0] %s", var.name);
            }),
            noMatchRet(() -> {
                return null;
            })
        );
    }
    public static String evalvar(ValueExp exp){
        return matchRet(exp,
            caseOfRet(ArrayExp.class, arr ->{
                return String.format("%s[%s]",
                        arr.variable.name,
                        evalvar(arr.index));
            }),
            caseOfRet(VariableExp.class, var ->{
                return var.name;
            }),
            caseOfRet(IntegerExp.class, num -> {
                return num.value + "";
            }),
            caseOfRet(BinaryExp.class, bin -> {
                return evalvar(bin.left) + bin.op + evalvar(bin.right);
            }),
            noMatchRet(() -> {
                return null;
            })
        );
    }
    public static void compile(String script, PrintWriter pw){
        LowHighComp.ModuleDef mod = LowHighComp.module().parse(script);
        pw.println("module " + mod.name.name + "(");
        
        Map<String, String> types = new HashMap<>();
        types.put("out", "output reg");
        types.put("in", "input");
        types.put("inout", "inout");
        
        pw.println(Stream.concat(
                Stream.of("  input clk"),
                mod.args.stream().map(arg -> 
                        String.format("  %s %s", types.get(arg.type), defvar(arg.arg)))
        ).collect(Collectors.joining(",\n")));
        pw.println(");");
        pw.println("  reg[15:0] cnt;");
        //変数定義
        Set<String> vars = new HashSet<>();
        mod.block.stream().forEach(stmt -> {
            if(stmt instanceof DefineStatement){
                DefineStatement def = (DefineStatement) stmt;
                printDefine(def, vars, pw);
            }else if(stmt instanceof LoopStatement){
                LoopStatement loop = (LoopStatement) stmt;
                if(!vars.contains(loop.var.name)){
                    vars.add(loop.var.name);
                    pw.printf("  reg[15:0] %s;%n", loop.var.name);
                }
                loop.block.forEach(stmtl -> {
                    if(stmtl instanceof DefineStatement){
                        printDefine((DefineStatement) stmtl, vars, pw);
                    }
                });
            }
        });
        
        pw.println("  always @(posedge clk) begin");
        pw.println("    case(cnt)");
        //処理出力
        int count = 0;
        for(Statement stmt : mod.block){
            if(stmt instanceof AssignmentStatement){
                AssignmentStatement assign = (AssignmentStatement) stmt;
                pw.printf("    %d:%n", ++count);
                pw.printf("      %s <= %s;%n", evalvar(assign.variable), evalvar(assign.value));
            }else if(stmt instanceof LoopStatement){
                LoopStatement loop = (LoopStatement) stmt;
                //ループ開始
                pw.printf("    %d:%n", ++count);
                pw.printf("      %s <= %d;%n", evalvar(loop.var), 0);
                int start = count;
                //ループ本体
                for(Statement stmtl : loop.block){
                    if(stmtl instanceof AssignmentStatement){
                        AssignmentStatement assign = (AssignmentStatement) stmtl;
                        pw.printf("    %d:%n", ++count);
                        pw.printf("      %s <= %s;%n", evalvar(assign.variable), evalvar(assign.value));
                    }
                }
                //ループ終了
                pw.printf("    %d:%n", ++count);
                pw.printf("      %s <= %<s + 1;%n", evalvar(loop.var));
                pw.printf("    %d:%n", ++count);
                pw.printf("      if(%s < %s) begin%n", evalvar(loop.var), evalvar(loop.count));
                pw.printf("        cnt <= %s;%n", start + 1);
                pw.printf("      end%n");
            }
        }
        pw.println("    endcase");
        pw.println("    cnt <= cnt + 1;");
        pw.println("  end");
        pw.println("endmodule");
        
    }

    private static void printDefine(DefineStatement def, Set<String> vars, PrintWriter pw) throws RuntimeException {
        String name = null;
        if(def.var instanceof VariableExp){
            name = ((VariableExp)def.var).name;
        }else if(def.var instanceof ArrayExp){
            name = ((ArrayExp)def.var).variable.name;
        }
        if(vars.contains(name)) throw new RuntimeException(name + "が２度定義されています");
        vars.add(name);
        pw.printf("  reg%s;%n", defvar(def.var));
    }
}
