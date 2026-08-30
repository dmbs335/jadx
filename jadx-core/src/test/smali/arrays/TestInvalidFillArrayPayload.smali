.class public final Larrays/TestInvalidFillArrayPayload;
.super Ljava/lang/Object;

.method public static test()[I
    .registers 2

    :method_start
    const/4 v0, 0x3
    new-array v0, v0, [I
    fill-array-data v0, :method_start
    return-object v0

    :orphan_payload
    .array-data 4
        0x1
        0x2
        0x3
    .end array-data
.end method
