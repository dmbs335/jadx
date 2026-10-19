.class public Ltypes/TestInvalidPrimitiveReturn;
.super Ljava/lang/Object;

.method public static intToFloat(I)F
    .locals 0
    return p0
.end method

.method public static longToDouble(J)D
    .locals 0
    return-wide p0
.end method

.method public static floatToInt(F)I
    .locals 0
    return p0
.end method

.method public static doubleToLong(D)J
    .locals 0
    return-wide p0
.end method

.method public static validFloat(F)F
    .locals 0
    return p0
.end method

.method public static validConvertedInt(I)F
    .locals 1
    int-to-float v0, p0
    return v0
.end method

.method public static validConvertedLong(J)D
    .locals 2
    long-to-double v0, p0
    return-wide v0
.end method

.method public static validConvertedFloat(F)I
    .locals 1
    float-to-int v0, p0
    return v0
.end method

.method public static validConvertedDouble(D)J
    .locals 2
    double-to-long v0, p0
    return-wide v0
.end method
