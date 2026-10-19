.class public Ltypes/TestInvalidFilledNewArrayNumeric;
.super Ljava/lang/Object;

.method public static intToFloatArray(I)[F
    .locals 1
    filled-new-array {p0}, [F
    move-result-object v0
    return-object v0
.end method

.method public static wideLongArray(J)[J
    .locals 1
    filled-new-array {p0}, [J
    move-result-object v0
    return-object v0
.end method

.method public static wideDoubleArrayRange(D)[D
    .locals 1
    filled-new-array/range {p0 .. p1}, [D
    move-result-object v0
    return-object v0
.end method

.method public static validFloatArray(F)[F
    .locals 1
    filled-new-array {p0}, [F
    move-result-object v0
    return-object v0
.end method

.method public static validConvertedInt(I)[F
    .locals 2
    int-to-float v0, p0
    filled-new-array {v0}, [F
    move-result-object v1
    return-object v1
.end method
