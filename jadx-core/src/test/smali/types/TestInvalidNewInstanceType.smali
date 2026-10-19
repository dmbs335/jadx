.class public Ltypes/TestInvalidNewInstanceType;
.super Ljava/lang/Object;

.method public static invalidArray()Ljava/lang/Object;
    .locals 1
    new-instance v0, [Ljava/lang/String;
    return-object v0
.end method

.method public static invalidPrimitive()Ljava/lang/Object;
    .locals 1
    new-instance v0, I
    return-object v0
.end method

.method public static invalidUninitialized()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    return-object v0
.end method

.method public static invalidUnused()V
    .locals 1
    new-instance v0, Ljava/lang/Object;
    return-void
.end method

.method public static valid()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method
