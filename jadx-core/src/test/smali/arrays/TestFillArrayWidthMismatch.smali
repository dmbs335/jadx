.class public final Larrays/TestFillArrayWidthMismatch;
.super Ljava/lang/Object;

.method public static bytePayloadIntoIntArray()[I
    .locals 2
    const/4 v0, 0x2
    new-array v1, v0, [I
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 1
        0x1
        0x2
    .end array-data
.end method

.method public static bytePayloadIntoIntParam([I)V
    .locals 0
    fill-array-data p0, :data
    return-void
    :data
    .array-data 1
        0x1
        0x2
    .end array-data
.end method

.method public static tooManyPayloadValues()[I
    .locals 2
    const/4 v0, 0x1
    new-array v1, v0, [I
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 4
        0x1
        0x2
    .end array-data
.end method

.method public static validIntPayload()[I
    .locals 2
    const/4 v0, 0x2
    new-array v1, v0, [I
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 4
        0x1
        0x2
    .end array-data
.end method

.method public static validShortPayload()[I
    .locals 2
    const/4 v0, 0x3
    new-array v1, v0, [I
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 4
        0x1
        0x2
    .end array-data
.end method

.method public static validIntParam([I)V
    .locals 0
    fill-array-data p0, :data
    return-void
    :data
    .array-data 4
        0x1
        0x2
    .end array-data
.end method

.method public static validBytePayload()[B
    .locals 2
    const/4 v0, 0x2
    new-array v1, v0, [B
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 1
        0x1
        0x2
    .end array-data
.end method

.method public static validFloatPayload()[F
    .locals 2
    const/4 v0, 0x2
    new-array v1, v0, [F
    fill-array-data v1, :data
    return-object v1
    :data
    .array-data 4
        0x3f800000
        0x40000000
    .end array-data
.end method
