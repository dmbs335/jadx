.class public Ltypes/TestInvalidPrimitiveArrayPut;
.super Ljava/lang/Object;

.method public static floatToIntArray([IF)V
    .locals 1

    const/4 v0, 0x0
    aput p1, p0, v0
    return-void
.end method

.method public static intToFloatArray([FI)V
    .locals 1

    const/4 v0, 0x0
    aput p1, p0, v0
    return-void
.end method

.method public static longToDoubleArray([DJ)V
    .locals 1

    const/4 v0, 0x0
    aput-wide p1, p0, v0
    return-void
.end method

.method public static validIntToIntArray([II)V
    .locals 1

    const/4 v0, 0x0
    aput p1, p0, v0
    return-void
.end method

.method public static validLongToLongArray([JJ)V
    .locals 1

    const/4 v0, 0x0
    aput-wide p1, p0, v0
    return-void
.end method
