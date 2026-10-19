.class public Ltypes/TestInvalidCheckCastOperand;
.super Ljava/lang/Object;

.method public static invalidUnrelatedClasses(Ljava/lang/Integer;)Ljava/lang/String;
    .locals 0

    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static invalidFinalToInterface(Ljava/lang/Integer;)Ljava/lang/CharSequence;
    .locals 0

    check-cast p0, Ljava/lang/CharSequence;
    return-object p0
.end method

.method public static invalidInterfaceToFinal(Ljava/lang/CharSequence;)Ljava/lang/Integer;
    .locals 0

    check-cast p0, Ljava/lang/Integer;
    return-object p0
.end method

.method public static invalidArrayToObject([I)Ljava/lang/String;
    .locals 0

    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static invalidObjectToArray(Ljava/lang/String;)[I
    .locals 0

    check-cast p0, [I
    return-object p0
.end method

.method public static invalidCrossDimension([I)[[Ljava/lang/Object;
    .locals 0

    check-cast p0, [[Ljava/lang/Object;
    return-object p0
.end method

.method public static invalidPrimitive(I)Ljava/lang/String;
    .locals 0

    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static validNonFinalToInterface(Ljava/lang/Number;)Ljava/lang/CharSequence;
    .locals 0

    check-cast p0, Ljava/lang/CharSequence;
    return-object p0
.end method

.method public static validSerializableToArray(Ljava/io/Serializable;)[I
    .locals 0

    check-cast p0, [I
    return-object p0
.end method

.method public static validNestedArrayToObject([[I)[Ljava/lang/Object;
    .locals 0

    check-cast p0, [Ljava/lang/Object;
    return-object p0
.end method

.method public static validNull()Ljava/lang/String;
    .locals 1

    const/4 v0, 0x0
    check-cast v0, Ljava/lang/String;
    return-object v0
.end method
