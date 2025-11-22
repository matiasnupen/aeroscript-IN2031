package no.uio.aeroscript.runtime;

import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;

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

    
}
