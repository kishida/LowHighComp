/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kis.lowhighcomp;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.Patterns;

/**
 *
 * @author naoki
 */
public class LowHighComp {
    public interface ValueExp{}
    public interface Statement{}
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class IntegerExp implements ValueExp{
        int value;
    }
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class VariableExp implements ValueExp{
        String name;
    }
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class ArrayExp implements ValueExp{
        VariableExp variable;
        ValueExp index;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class DefineStatement implements Statement{
        ValueExp var;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class BinaryExp implements ValueExp{
        ValueExp left;
        ValueExp right;
        String op;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class AssignmentStatement implements Statement{
        ValueExp variable;
        ValueExp value;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class LoopStatement implements Statement{
        VariableExp var;
        ValueExp count;
        List<Statement> block;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class Argument{
        String type;
        ValueExp arg;
    }
    
    @AllArgsConstructor
    @ToString @EqualsAndHashCode
    public static class ModuleDef{
        VariableExp name;
        List<Argument> args;
        List<Statement> block;
    }
    // whitespace ::= [ ]*
    public static Parser<Void> whitespace(){
        return Scanners.pattern(Patterns.among(" \t").many(), "whitespace");
    }
    public static Parser<Void> whitespacesep(){
        return Scanners.pattern(Patterns.among(" \t").many1(), "whitespace separator");
    }
    
    // newline ::= \\n
    public static Parser<Void> newline(){
        return Scanners.pattern(Patterns.among("\n\r").many1(), "newline");
    }
    // integer ::= [0-9]+
    public static Parser<IntegerExp> integer(){
        return whitespace().next(Scanners.pattern(
                Patterns.range('1', '9').next(Patterns.range('0', '9').many()), "integer").source()
                .map(s -> new IntegerExp(Integer.valueOf(s))));
    }
    // variable ::= [a-z]+
    public static Parser<VariableExp> identifier(){
        return whitespace().next(Scanners.pattern(
                Patterns.range('a', 'z').next(Patterns.or(Patterns.range('0', '9'), 
                        Patterns.range('a', 'z')).many()), "identifier").source()
                .map(id -> new VariableExp(id)));
    }
    // array ::= variable ("[" (variable | integer) "]")?
    public static Parser<ValueExp> array(){
        return identifier().next(v -> Scanners.string("[").next(Parsers.or(identifier(), integer())
                .next(val -> Scanners.string("]").map(vd -> val))).optional()
                .map(val -> val != null ? new ArrayExp( v, val) : v));
    }
    // value ::= integer | array
    public static Parser<ValueExp> value(){
        return Parsers.or(integer(), array());
    }
    
    // define :: = "var" array
    public static Parser<DefineStatement> define(){
        return whitespace().next(Scanners.string("var")).next(whitespacesep())
                .next(array()).map(ar -> new DefineStatement(ar));
    }
    
    // expression :: = value (("+" | "-" | "*" | "/" | "%") value)?
    public static Parser<ValueExp> expression(){
        return value().next(left -> whitespace()
            .next(Scanners.among("+-*/%").source())
            .next(op -> value().map(right ->
                    new BinaryExp(left, right, op))).optional()
            .map(bin -> bin != null ? bin : left));
    }
    // assignment :: = array "=" expression
    public static Parser<AssignmentStatement> assignment(){
        return array().next(ar -> whitespace()
                .next(Scanners.string("="))
                .next(expression())
                .map(val -> new AssignmentStatement(ar, val)));
    }
    // loop ::= "loop" integer "for" variable (assignment | define)* "end"
    public static Parser<LoopStatement> loop(){
        return whitespace().next(Scanners.string("loop")).next(whitespacesep()).next(value()).next(val ->
                whitespacesep().next(Scanners.string("for").next(whitespacesep()).next(identifier()).next(id -> 
                whitespace().next(newline()).next(Parsers.or(assignment(), define()).sepEndBy(newline()).next(block ->
                        whitespace().next(Scanners.string("end")).map(vd -> new LoopStatement(id, val, block)))))));
    }
    
    public static Parser<Argument> argument(){
        return whitespace().next(
                Parsers.or(Scanners.string("in").next(Scanners.string("out").optional()), 
                        Scanners.string("out")).source())
                .next(type -> whitespacesep().next(array()).map(var -> new Argument(type, var)));
    }
    public static Parser<ModuleDef> module(){
        return whitespace().next(Scanners.string("module")).next(whitespacesep())
                .next(identifier()).next(nm -> 
                        Parsers.between(Scanners.string("("), argument().sepBy(Scanners.string(",")), Scanners.string(")"))
                                .next(args -> newline().next(Parsers.or(assignment(), define(), loop()).sepEndBy(newline()))
                                        .next(block -> whitespace().next(Scanners.string("end"))
                                                .next(newline().optional()).map(vd -> new ModuleDef(nm, args, block)))));
    }

}
