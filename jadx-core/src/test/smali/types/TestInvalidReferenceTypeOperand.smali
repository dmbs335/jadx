.class public Ltypes/TestInvalidReferenceTypeOperand;
.super Ljava/lang/Object;

.method public static checkCastPrimitive(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    check-cast p0, I
    return-object p0
.end method

.method public static instanceOfPrimitive(Ljava/lang/Object;)Z
    .locals 1
    instance-of v0, p0, I
    return v0
.end method

.method public static validCheckCast(Ljava/lang/Object;)Ljava/lang/String;
    .locals 0
    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static validInstanceOf(Ljava/lang/Object;)Z
    .locals 1
    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method
