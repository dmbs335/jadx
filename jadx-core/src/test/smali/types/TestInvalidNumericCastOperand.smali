.class public Ltypes/TestInvalidNumericCastOperand;
.super Ljava/lang/Object;

.field public static doubleValue:D

.method public static getFloat()F
    .locals 1

    const/high16 v0, 0x3f800000
    return v0
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static intToFloatFromFloat(F)F
    .locals 1

    int-to-float v0, p0
    return v0
.end method

.method public static floatToIntFromInt(I)I
    .locals 1

    float-to-int v0, p0
    return v0
.end method

.method public static longToDoubleFromDouble(D)D
    .locals 2

    long-to-double v0, p0
    return-wide v0
.end method

.method public static intToFloatFromInvoke()F
    .locals 1

    invoke-static {}, Ltypes/TestInvalidNumericCastOperand;->getFloat()F
    move-result v0
    int-to-float v0, v0
    return v0
.end method

.method public static floatToIntFromArray([I)I
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    float-to-int v1, v1
    return v1
.end method

.method public static longToDoubleFromField()D
    .locals 2

    sget-wide v0, Ltypes/TestInvalidNumericCastOperand;->doubleValue:D
    long-to-double v0, v0
    return-wide v0
.end method

.method public static validIntToFloat(I)F
    .locals 1

    int-to-float v0, p0
    return v0
.end method

.method public static validLongToDouble(J)D
    .locals 2

    long-to-double v0, p0
    return-wide v0
.end method

.method public static validIntToFloatFromInvoke()F
    .locals 1

    invoke-static {}, Ltypes/TestInvalidNumericCastOperand;->getInt()I
    move-result v0
    int-to-float v0, v0
    return v0
.end method

.method public static validIntToFloatFromArray([I)F
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    int-to-float v1, v1
    return v1
.end method
