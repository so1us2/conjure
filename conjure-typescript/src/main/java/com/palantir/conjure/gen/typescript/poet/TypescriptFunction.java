/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFunction extends Emittable {
    TypescriptFunctionBody functionBody();
    TypescriptFunctionSignature functionSignature();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented("public ");
        functionSignature().emit(writer);
        writer.write(" ");
        functionBody().emit(writer);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptFunction.Builder {}
}