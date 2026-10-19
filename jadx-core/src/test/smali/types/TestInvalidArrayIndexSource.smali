.class public Ltypes/TestInvalidArrayIndexSource;
.super Ljava/lang/Object;

.method public static floatSize(F)[I
    .locals 1
    new-array v0, p0, [I
    return-object v0
.end method

.method public static floatIndexGet([IF)I
    .locals 1
    aget v0, p0, p1
    return v0
.end method

.method public static floatIndexPut([IFI)V
    .locals 0
    aput p2, p0, p1
    return-void
.end method

.method public static validIndex([II)I
    .locals 1
    aget v0, p0, p1
    return v0
.end method
