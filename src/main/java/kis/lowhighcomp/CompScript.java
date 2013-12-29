/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import kis.lowhighcomp.LowHighComp.*;
import static kis.lowhighcomp.ObjectPatternMatch.*;
/**
 *
 * @author naoki
 */
public class CompScript {
    static Map<String, Object> variable = new HashMap<>();
    static int visit(ValueExp v){
        return matchRet(v,
            caseOfRet(IntegerExp.class, ie -> {
                return ie.value;
            }),
            caseOfRet(VariableExp.class, var -> {
                if(variable.containsKey(var.name)){
                    Object value = variable.get(var.name);
                    if(value instanceof Integer){
                        return (int)value;
                    }else{
                        throw new RuntimeException(var.name + "は整数ではありません");
                    }
                }else{
                    throw new RuntimeException(var.name + "がありません");
                }
            }),
            caseOfRet(ArrayExp.class, arr -> {
                if(variable.containsKey(arr.variable.name)){
                    Object value = variable.get(arr.variable.name);
                    if(value instanceof int[]){
                        int idx = visit(arr.index);
                        return ((int[])value)[idx];
                    }else{
                        throw new RuntimeException(arr.variable.name + "が配列ではありません");
                    }
                }else{
                    throw new RuntimeException(arr.variable.name + "がありません");
                }
            }),
            caseOfRet(BinaryExp.class, bin -> {
                int left = visit(bin.left);
                int right = visit(bin.right);
                switch(bin.op){
                    case "+":
                        return left + right;
                    case "-":
                        return left - right;
                    case "*":
                        return left * right;
                    case "/":
                        return left / right;
                    case "%":
                        return left % right;
                    default:
                        throw new RuntimeException("演算子" + bin.op + "には対応していません");
                }
            }),
            noMatchRet(() -> {
                throw new RuntimeException(v.getClass().getName() + "には対応していません");
            })
        );
    }
    
    static void visit(Statement o){
        match(o,
            caseOf(DefineStatement.class, df -> {
                match(df.var, 
                    caseOf(VariableExp.class, var -> {
                        //変数定義
                        if(variable.containsKey(var.name)){
                            throw new RuntimeException(var.name + "はすでに定義されています");
                        }
                        variable.put(var.name, 0);
                    }),
                    caseOf(ArrayExp.class, arr ->{
                        //配列定義
                        if(variable.containsKey(arr.variable.name)){
                            throw new RuntimeException(arr.variable.name + "はすでに定義されています");
                        }
                        int size = visit(arr.index);
                        variable.put(arr.variable.name, new int[size]);
                    })
                );
            }),
            caseOf(LoopStatement.class, loop -> {
                if(variable.containsKey(loop.var.name)){
                    throw new RuntimeException(loop.var.name + "はすでに定義されています");
                }
                int count = visit(loop.count);
                for(int i = 0; i < count; ++i){
                    variable.put(loop.var.name, i);
                    loop.block.stream().forEach(CompScript::visit);
                }
                variable.remove(loop.var.name);
            }),
            caseOf(AssignmentStatement.class, assign -> {
                match(assign.variable, 
                    caseOf(VariableExp.class, var -> {
                        //変数割り当て
                        if(variable.containsKey(var.name)){
                            if(variable.get(var.name) instanceof Integer){
                                variable.put(var.name, visit(assign.value));
                            }else{
                                throw new RuntimeException(var.name + "は整数ではありません");
                            }
                        }else{
                            throw new RuntimeException(var.name + "は定義されていません");
                        }
                    }),
                    caseOf(ArrayExp.class, arr ->{
                        //配列割り当て
                        if(variable.containsKey(arr.variable.name)){
                            Object var = variable.get(arr.variable.name);
                            if(var instanceof int[]){
                                ((int[])var)[visit(arr.index)] = visit(assign.value);
                            }else{
                                throw new RuntimeException(arr.variable.name + "は配列ではありません");
                            }
                        }
                    })
                );
            })
        );
    }
    
    public static Map<String, Object> module(ModuleDef mod, Object... args){
        variable.clear();
        IntStream.range(0, mod.args.size()).forEach(i -> {
            match(mod.args.get(i).arg,
                caseOf(VariableExp.class, var -> {
                    if(args[i] instanceof Integer){
                        variable.put(var.name, args[i]);
                    }else{
                        throw new RuntimeException(i + "番目が整数ではありません");
                    }
                }),
                caseOf(ArrayExp.class, arr -> {
                    if(args[i] instanceof int[]){
                        if(visit(arr.index) != ((int[])args[i]).length){
                            System.out.println(i + "番目の配列サイズが違います");
                        }
                        variable.put(arr.variable.name, args[i]);
                    }else{
                        throw new RuntimeException(i + "番目が配列ではありません");
                    }
                }),
                noMatch(() -> {
                    throw new RuntimeException(mod.args.get(i).arg.getClass().getName() + "の引数には対応していません");
                })
            );
        });
        mod.block.stream().forEach(CompScript::visit);
        
        Map<String, Object> result = new HashMap<>();
        mod.args.stream().forEach(arg -> {
            match(arg.arg,
                caseOf(VariableExp.class, var -> {
                    result.put(var.name, variable.get(var.name));
                }),
                caseOf(ArrayExp.class, arr -> {
                    result.put(arr.variable.name, variable.get(arr.variable.name));
                })
            );
        });
        
        return result;
    }
    
    public static Map<String,Object> eval(String script, Object... args){
        ModuleDef mod = LowHighComp.module().parse(script);
        return module(mod, args);
    }
}
