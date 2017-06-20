/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.builtin.SafeLongType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ConjurePackages;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ForeignReferenceType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.palantir.conjure.lib.SafeLong;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Maps the conjure type into the 'standard' java type i.e.
 * the type one would use in beans/normal variables (as opposed
 * to e.g. service definitions).
 */
public final class DefaultClassNameVisitor implements ClassNameVisitor {

    private final TypesDefinition types;

    DefaultClassNameVisitor(TypesDefinition types) {
        this.types = types;
    }

    @Override
    public TypeName visit(AnyType type) {
        return ClassName.get(Object.class);
    }

    @Override
    public TypeName visit(ListType type) {
        TypeName itemType = type.itemType().visit(this).box();
        return ParameterizedTypeName.get(ClassName.get(java.util.List.class), itemType);
    }

    @Override
    public TypeName visit(MapType type) {
        return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                type.keyType().visit(this).box(),
                type.valueType().visit(this).box());
    }

    @Override
    public TypeName visit(OptionalType type) {
        if (type.itemType() instanceof PrimitiveType) {
            // special handling for primitive optionals with Java 8
            switch ((PrimitiveType) type.itemType()) {
                case DOUBLE:
                    return ClassName.get(OptionalDouble.class);
                case INTEGER:
                    return ClassName.get(OptionalInt.class);
                case BOOLEAN:
                    // no OptionalBoolean type
                case STRING:
                default:
                    // treat normally
            }
        }

        TypeName itemType = type.itemType().visit(this);
        if (itemType.isPrimitive()) {
            // Safe for primitives (e.g. Booleans with Java 8)
            itemType = itemType.box();
        }
        return ParameterizedTypeName.get(ClassName.get(Optional.class), itemType);
    }

    @Override
    public TypeName visit(PrimitiveType type) {
        switch (type) {
            case STRING:
                return ClassName.get(String.class);
            case DOUBLE:
                return TypeName.DOUBLE;
            case INTEGER:
                return TypeName.INT;
            case BOOLEAN:
                return TypeName.BOOLEAN;
            default:
                throw new IllegalStateException("Unknown primitive type: " + type);
        }
    }

    @Override
    public TypeName visit(LocalReferenceType type) {
        // Types without namespace are either defined locally in this conjure definition, or raw imports.
        BaseObjectTypeDefinition baseType = types.definitions().objects().get(type.type());
        if (baseType != null) {
            ConjurePackage conjurePackage = ConjurePackages.getPackage(baseType.conjurePackage(),
                    types.definitions().defaultConjurePackage(), type.type());
            return ClassName.get(conjurePackage.name(), type.type().name());
        } else {
            ExternalTypeDefinition depType = types.imports().get(type.type());
            checkNotNull(depType, "Unable to resolve type %s", type.type());
            return ClassName.bestGuess(depType.external().get("java"));
        }
    }

    @Override
    public TypeName visit(ForeignReferenceType type) {
        return ClassName.get(
                types.getImportsForRefNameSpace(type).getPackageForImportedType(type).name(),
                type.type().name());
    }

    @Override
    public TypeName visit(SetType type) {
        TypeName itemType = type.itemType().visit(this).box();
        return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), itemType);
    }

    @Override
    public TypeName visit(BinaryType type) {
        return ClassName.get(ByteBuffer.class);
    }

    @Override
    public TypeName visit(SafeLongType type) {
        return ClassName.get(SafeLong.class);
    }

    @Override
    public TypeName visit(DateTimeType type) {
        return ClassName.get(ZonedDateTime.class);
    }
}
