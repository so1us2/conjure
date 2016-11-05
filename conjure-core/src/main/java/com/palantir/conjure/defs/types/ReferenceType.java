/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ReferenceType extends ConjureType {

    @JsonProperty("type")
    String type();

    static ReferenceType of(String type) {
        Optional<PrimitiveType> primitiveType = PrimitiveType.fromTypeString(type);
        if (primitiveType.isPresent()) {
            return primitiveType.get();
        }
        return ImmutableReferenceType.builder().type(type).build();
    }

}