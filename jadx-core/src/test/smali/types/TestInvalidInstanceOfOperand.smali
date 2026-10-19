.class public Ltypes/TestInvalidInstanceOfOperand;
.super Ljava/lang/Object;

.method public static invalidPrimitive(I)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static validReference(Ljava/lang/Object;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static invalidInvoke()Z
    .locals 2

    invoke-static {}, Ltypes/TestInvalidInstanceOfOperand;->getInt()I
    move-result v0
    instance-of v1, v0, Ljava/lang/String;
    return v1
.end method

.method public static validNull()Z
    .locals 2

    const/4 v0, 0x0
    instance-of v1, v0, Ljava/lang/String;
    return v1
.end method

.method public static invalidUnrelatedFinal(Ljava/lang/Integer;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static validSubtype(Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidUnrelatedClasses(Ljava/lang/Number;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static validCrossInterfaces(Ljava/io/Serializable;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidObjectArrays([Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/Integer;
    return v0
.end method

.method public static invalidPrimitiveArrays([I)Z
    .locals 1

    instance-of v0, p0, [J
    return v0
.end method

.method public static validCovariantArray([Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/CharSequence;
    return v0
.end method

.method public static validInterfaceArrays([Ljava/io/Serializable;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidNestedObjectArrays([[Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, [[Ljava/lang/Integer;
    return v0
.end method

.method public static invalidNestedPrimitiveArray([[I)Z
    .locals 1

    instance-of v0, p0, [[Ljava/lang/Object;
    return v0
.end method

.method public static validNestedAsObjectArray([[I)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/Object;
    return v0
.end method

.method public static invalidFinalToInterface(Ljava/lang/Integer;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidInterfaceToFinal(Ljava/lang/CharSequence;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/Integer;
    return v0
.end method

.method public static validNonFinalToInterface(Ljava/lang/Number;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/CharSequence;
    return v0
.end method

.method public static validInterfaceToNonFinal(Ljava/lang/CharSequence;)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/Number;
    return v0
.end method

.method public static validFinalToImplementedInterface(Ljava/lang/Integer;)Z
    .locals 1

    instance-of v0, p0, Ljava/io/Serializable;
    return v0
.end method

.method public static invalidFinalToInterfaceArray([Ljava/lang/Integer;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidInterfaceToFinalArray([Ljava/lang/CharSequence;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/Integer;
    return v0
.end method

.method public static validNonFinalToInterfaceArray([Ljava/lang/Number;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/CharSequence;
    return v0
.end method

.method public static invalidPrimitiveToNestedArray([I)Z
    .locals 1

    instance-of v0, p0, [[Ljava/lang/Object;
    return v0
.end method

.method public static invalidObjectToNestedArray([Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, [[Ljava/lang/Object;
    return v0
.end method

.method public static invalidNestedArrayToFinalObject([[Ljava/lang/Object;)Z
    .locals 1

    instance-of v0, p0, [Ljava/lang/String;
    return v0
.end method

.method public static invalidClassArrayToNestedPrimitive([Ljava/lang/Number;)Z
    .locals 1

    instance-of v0, p0, [[I
    return v0
.end method

.method public static validObjectToNestedPrimitive([Ljava/lang/Object;)Z
    .locals 1

    instance-of v0, p0, [[I
    return v0
.end method

.method public static validSerializableToNestedPrimitive([Ljava/io/Serializable;)Z
    .locals 1

    instance-of v0, p0, [[I
    return v0
.end method

.method public static validCloneableToNestedPrimitive([Ljava/lang/Cloneable;)Z
    .locals 1

    instance-of v0, p0, [[I
    return v0
.end method

.method public static validNestedPrimitiveAsSerializable([[I)Z
    .locals 1

    instance-of v0, p0, [Ljava/io/Serializable;
    return v0
.end method

.method public static invalidObjectToPrimitiveArray(Ljava/lang/String;)Z
    .locals 1

    instance-of v0, p0, [I
    return v0
.end method

.method public static invalidPrimitiveArrayToObject([I)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static invalidNonFinalClassToArray(Ljava/lang/Number;)Z
    .locals 1

    instance-of v0, p0, [I
    return v0
.end method

.method public static invalidArrayToInterface([I)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/CharSequence;
    return v0
.end method

.method public static validObjectToArray(Ljava/lang/Object;)Z
    .locals 1

    instance-of v0, p0, [I
    return v0
.end method

.method public static validSerializableToArray(Ljava/io/Serializable;)Z
    .locals 1

    instance-of v0, p0, [I
    return v0
.end method

.method public static validArrayAsCloneable([I)Z
    .locals 1

    instance-of v0, p0, Ljava/lang/Cloneable;
    return v0
.end method
