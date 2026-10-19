.class public Ltypes/TestInvalidIfPrimitiveSource;
.super Ljava/lang/Object;

.method public static floatEqZero(F)I
    .locals 1
    if-eqz p0, :zero
    const/4 v0, 0x1
    return v0
    :zero
    const/4 v0, 0x0
    return v0
.end method

.method public static floatEqFloat(FF)I
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method

.method public static floatLtFloat(FF)I
    .locals 1
    if-lt p0, p1, :less
    const/4 v0, 0x0
    return v0
    :less
    const/4 v0, 0x1
    return v0
.end method

.method public static validIntEqZero(I)I
    .locals 1
    if-eqz p0, :zero
    const/4 v0, 0x1
    return v0
    :zero
    const/4 v0, 0x0
    return v0
.end method

.method public static validFloatCmp(FF)I
    .locals 1
    cmpg-float v0, p0, p1
    if-ltz v0, :less
    const/4 v0, 0x0
    return v0
    :less
    const/4 v0, 0x1
    return v0
.end method
