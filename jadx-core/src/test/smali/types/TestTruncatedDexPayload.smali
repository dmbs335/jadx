.class public Ltypes/TestTruncatedDexPayload;
.super Ljava/lang/Object;

.method public static run()[I
    .locals 1
    const/4 v0, 0x1
    new-array v0, v0, [I
    fill-array-data v0, :payload
    return-object v0
    :payload
    .array-data 4
        0x7
    .end array-data
.end method
