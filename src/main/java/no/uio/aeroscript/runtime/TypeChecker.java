package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.error.TypeError;

public class TypeChecker extends AeroScriptBaseVisitor<TypeChecker.Type> {

    public enum Type {
        NUM, // Float type
        POINT, // Point type
        RANGE, // Range type
        VOID
    }

    private final AeroScriptParser.ProgramContext programContext;

    public TypeChecker(AeroScriptParser.ProgramContext programContext) {
        this.programContext = programContext;
    }

    public void check() {
        visitProgram(programContext);
    }

    @Override
    public Type visitProgram(AeroScriptParser.ProgramContext ctx) {
        if (ctx == null) return null;
        for (AeroScriptParser.ModeDeclContext modeCtx : ctx.modeDecl()) {
            visitModeDecl(modeCtx);
        }
        return null;
    }

    @Override
    public Type visitModeDecl(AeroScriptParser.ModeDeclContext ctx) { 
        for (AeroScriptParser.StatementContext stmtCtx : ctx.statement()) {
            visitStatement(stmtCtx);
        }
        return Type.VOID;
    }

    @Override
    public Type visitStatement(AeroScriptParser.StatementContext ctx) {
        if (ctx.action() != null) {
            return visitAction(ctx.action());
        } else if (ctx.reaction() != null) {
            return visitReaction(ctx.reaction());
        }
        return Type.VOID;
    }

    @Override
    public Type visitAction(AeroScriptParser.ActionContext ctx) {
        // Check action-specific types
        if (ctx.acDock() != null) {
            return visitAcDock(ctx.acDock());
        } else if (ctx.acMove() != null) {
            return visitAcMove(ctx.acMove());
        } else if (ctx.acTurn() != null) {
            return visitAcTurn(ctx.acTurn());
        } else if (ctx.acAscend() != null) {
            return visitAcAscend(ctx.acAscend());
        } else if (ctx.acDescend() != null) {
            return visitAcDescend(ctx.acDescend());
        }

        // Check optional modifiers (FOR // AT SPEED)
        if (ctx.getChildCount() > 1) {
            if (ctx.FOR() != null) {
                Type durationType = visitExpression((AeroScriptParser.ExpressionContext) ctx.getChild(2));            
                if (durationType != Type.NUM) {
                throw new TypeError("Duration in FOR clause must be a number, got: " + durationType + " in: " + ctx.getText());
            }
        }
            if (ctx.AT() != null) {
                Type speedType = visitExpression((AeroScriptParser.ExpressionContext) ctx.getChild(3));            
                if (speedType != Type.NUM) {
                    throw new TypeError("Speed in AT SPEED clause must be a number, got: " + speedType + " in: " + ctx.getText());
                }
            }
        }
        return Type.VOID;
    }

    @Override
    public Type visitAcDock(AeroScriptParser.AcDockContext ctx) {
        return Type.VOID; // Has no type constraints
    }

    @Override
    public Type visitAcMove(AeroScriptParser.AcMoveContext ctx) {
        if (ctx.point() != null) {
            Type pointType = visitPoint(ctx.point());
            if (pointType != Type.POINT) {
                throw new TypeError("Move action requires a POINT type, got: " + pointType + " in: " + ctx.getText());
            }
        } else if (ctx.BY() != null) {
            // Needs no additional type checking, must be a NUM-type already checked in expression
        }
        return Type.VOID;
    }

    @Override
    public Type visitAcTurn(AeroScriptParser.AcTurnContext ctx) {
        // TURN requires a NUM type for degrees
        Type degreeType = visitExpression(ctx.expression());
        if (degreeType != Type.NUM) {
            throw new TypeError("Turn action requires a NUM type for degrees, got: " + degreeType + " in: " + ctx.getText());
        }
        return Type.VOID;
    }

    @Override
    public Type visitAcAscend(AeroScriptParser.AcAscendContext ctx) {
        // ASCEND requires a NUM type for height
        Type numberAscendingType = visitExpression(ctx.expression());
        if (numberAscendingType != Type.NUM) {
            throw new TypeError("Ascend action requires a NUM type for height, got: " + numberAscendingType + " in: " + ctx.getText());
        }
        return Type.VOID;
    }

    @Override
    public Type visitAcDescend(AeroScriptParser.AcDescendContext ctx) {
        if (ctx.BY() != null) {
            // DESCEND BY requires a NUM type
            Type numberDescendingType = visitExpression(ctx.expression());
            if (numberDescendingType != Type.NUM) {
                throw new TypeError("Descend action with BY requires a NUM type for height, got: " + numberDescendingType + " in: " + ctx.getText());
            }
        }
        return Type.VOID;
    }

    @Override
    public Type visitReaction(AeroScriptParser.ReactionContext ctx) {
        // Reactios just trigger mode transitions, no type checking needed
        visitEvent(ctx.event());
        return Type.VOID;
    }

    @Override
    public Type visitEvent(AeroScriptParser.EventContext ctx) {
        // Events have no type constraints
        return Type.VOID;
    }

    @Override
    public Type visitExpression(AeroScriptParser.ExpressionContext ctx) {
       return switch (ctx) {
            case AeroScriptParser.PlusExpContext pex -> visitPlusExp(pex);
            case AeroScriptParser.MinusExpContext pex -> visitMinusExp(pex);
            case AeroScriptParser.TimesExpContext pex -> visitTimesExp(pex);
            case AeroScriptParser.NumExpContext pex -> visitNumExp(pex);
            case AeroScriptParser.NegExpContext pex -> visitNegExp(pex);
            case AeroScriptParser.ParentExpContext pex -> visitParentExp(pex);
            case AeroScriptParser.RangeExpContext pex -> visitRangeExp(pex);
            case AeroScriptParser.PointExpContext pex -> visitPointExp(pex);
            default -> throw new TypeError("Unknown expression type: " + ctx.getText());
        };
    }

    @Override
    public Type visitPlusExp(AeroScriptParser.PlusExpContext ctx) {
        Type leftType = visitExpression(ctx.left);
        Type rightType = visitExpression(ctx.right);
        if (leftType != Type.NUM || rightType != Type.NUM) {
            throw new TypeError("Plus expression requires NUM types, got: " + leftType + " and " + rightType + " in: " + ctx.getText());
        }
        return Type.NUM;
    }

    @Override
    public Type visitMinusExp(AeroScriptParser.MinusExpContext ctx) {
        Type leftType = visitExpression(ctx.left);
        Type rightType = visitExpression(ctx.right);
        if (leftType != Type.NUM || rightType != Type.NUM) {
            throw new TypeError("Minus expression requires NUM types, got: " + leftType + " and " + rightType + " in: " + ctx.getText());
        }
        return Type.NUM;
    }

    @Override
    public Type visitTimesExp(AeroScriptParser.TimesExpContext ctx) {
        Type leftType = visitExpression(ctx.left);
        Type rightType = visitExpression(ctx.right);
        if (leftType != Type.NUM || rightType != Type.NUM) {
            throw new TypeError("Times expression requires NUM types, got: " + leftType + " and " + rightType + " in: " + ctx.getText());
        }
        return Type.NUM;
    }

    @Override
    public Type visitNumExp(AeroScriptParser.NumExpContext ctx) {
        return Type.NUM;
    }

    @Override
    public Type visitNegExp(AeroScriptParser.NegExpContext ctx) {
        Type exprType = visitExpression(ctx.expression());
        if (exprType != Type.NUM) {
            throw new TypeError("Negation expression requires NUM type, got: " + exprType + " in: " + ctx.getText());
        }
        return Type.NUM;
    }

    @Override
    public Type visitParentExp(AeroScriptParser.ParentExpContext ctx) {
        return visitExpression(ctx.expression());
    }
    @Override
    public Type visitRangeExp(AeroScriptParser.RangeExpContext ctx) {
        return visitRange(ctx.range());
    }
    @Override
    public Type visitPointExp(AeroScriptParser.PointExpContext ctx) {
        return visitPoint(ctx.point());
    }

    @Override
    public Type visitPoint(AeroScriptParser.PointContext ctx) {
        Type xType = visitExpression(ctx.left);
        Type yType = visitExpression(ctx.right);
        if (xType != Type.NUM || yType != Type.NUM) {
            throw new TypeError("Point requires NUM types for x and y coordinates, got: " + xType + " and " + yType + " in: " + ctx.getText());
        }
        return Type.POINT;
    }

    @Override
    public Type visitRange(AeroScriptParser.RangeContext ctx) {
        Type startType = visitExpression(ctx.left);
        Type endType = visitExpression(ctx.right);
        if (startType != Type.NUM || endType != Type.NUM) {
            throw new TypeError("Range requires NUM types for start and end, got: " + startType + " and " + endType + " in: " + ctx.getText());
        }
        return Type.RANGE;
    }

}
