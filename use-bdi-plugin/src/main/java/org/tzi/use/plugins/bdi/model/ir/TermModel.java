package org.tzi.use.plugins.bdi.model.ir;

/** Jason-independent normalized term tree. */
public sealed interface TermModel
        permits LiteralTermModel, VariableTermModel, NumberTermModel, StringTermModel,
        CompoundTermModel, ListTermModel, SetTermModel, ArithmeticTermModel,
        UnsupportedTermModel {
    SourceSpan sourceSpan();

    String render();
}
