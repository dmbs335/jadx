.class public Ltypes/TestInvalidUnaryNumericOperand;
.super Ljava/lang/Object;

.method public static negIntFromFloat(F)I
    .locals 1

    neg-int v0, p0
    return v0
.end method

.method public static negFloatFromInt(I)F
    .locals 1

    neg-float v0, p0
    return v0
.end method

.method public static notLongFromInt(I)J
    .locals 2

    not-long v0, p0
    return-wide v0
.end method

.method public static negIntFromFloatArray([F)I
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    neg-int v1, v1
    return v1
.end method

.method public static notLongFromIntArray([I)J
    .locals 3

    const/4 v0, 0x0
    aget v1, p0, v0
    not-long v1, v1
    return-wide v1
.end method

.method public static validNegInt(I)I
    .locals 1

    neg-int v0, p0
    return v0
.end method

.method public static validNegFloat(F)F
    .locals 1

    neg-float v0, p0
    return v0
.end method

.method public static validNotLong(J)J
    .locals 2

    not-long v0, p0
    return-wide v0
.end method

.method public static validNegIntFromArray([I)I
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    neg-int v1, v1
    return v1
.end method
