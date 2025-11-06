package no.uio.aeroscript.runtime;

import no.uio.aeroscript.ast.expr.Expression;

import org.antlr.v4.runtime.tree.ParseTree;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;

enum AeroType {
    NUM, POINT, RANGE
}

public class TypeChecker extends AeroScriptBaseVisitor<AeroType> {

    /** Methods for the type-rules
     * @param visitNumExp(ctx) checks if the ctx is a Number type
     * @param visitPointExp(ctx) checks if the ctx is a Point type, using number types
     * @param visitRangeExp(ctx) checks if the ctx is a Range type, using number types
     */
    @Override
    public AeroType visitNumExp(AeroScriptParser.NumExpContext ctx) {
        return AeroType.NUM;
    }

    @Override
    public AeroType visitPointExp(AeroScriptParser.PointExpContext ctx) {
        AeroType leftType = visit(ctx.point().left);
        AeroType rightType = visit(ctx.point().right);
        if (leftType != AeroType.NUM || rightType != AeroType.NUM) {
            throw new TypeError("Point expressions must be numeric types");
        }
        return AeroType.POINT;
    }

    @Override
    public AeroType visitRangeExp(AeroScriptParser.RangeExpContext ctx) {
        AeroType startType = visit(ctx.range().left);
        AeroType endType = visit(ctx.range().right);
        if (startType != AeroType.NUM || endType != AeroType.NUM) {
            throw new TypeError("Range expressions must be numeric types");
        }
        return AeroType.RANGE;
    }

    /** Methods for binary -and unary operators
     * @param visitPlusExp(ctx) checks if the ctx is a Plus expression, using number types
     * @param visitMinusExp(ctx) checks if the ctx is a Minus expression, using number types
     * @param visitTimesExp(ctx) checks if the ctx is a Multiplication expression, using number types
     * @param visitNegExp(ctx) checks if the ctx is a Negation expression, using number types
     */

    @Override
    public AeroType visitPlusExp(AeroScriptParser.PlusExpContext ctx) {
        AeroType leftType = visit(ctx.left);
        AeroType rightType = visit(ctx.right);
        if (leftType != AeroType.NUM || rightType != AeroType.NUM) {
            throw new TypeError("Plus operator requires numeric types");
        }
        return AeroType.NUM;
    }

    @Override
    public AeroType visitMinusExp(AeroScriptParser.MinusExpContext ctx) {
        AeroType leftType = visit(ctx.left);
        AeroType rightType = visit(ctx.right);
        if (leftType != AeroType.NUM || rightType != AeroType.NUM) {
            throw new TypeError("Minus operator requires numeric types");
        }
        return AeroType.NUM;
    }

    @Override
    public AeroType visitTimesExp(AeroScriptParser.TimesExpContext ctx) {
        AeroType leftType = visit(ctx.left);
        AeroType rightType = visit(ctx.right);
        if (leftType != AeroType.NUM || rightType != AeroType.NUM) {
            throw new TypeError("Multiplication operator requires numeric types");
        }
        return AeroType.NUM;    
    }
    
    @Override
    public AeroType visitNegExp(AeroScriptParser.NegExpContext ctx) {
        AeroType exprType = visit(ctx.expression());
        if (exprType != AeroType.NUM) {
            throw new TypeError("Negation operator requires a numeric type");
        }
        return AeroType.NUM;
    }

}