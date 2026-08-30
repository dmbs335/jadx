.class public Ltypes/TestSinglePrimitivePhi;
.super Ljava/lang/Object;

.method public static hash(ZI)I
    .registers 4

    move v0, p0
    if-eqz p0, :converted
    const/4 v0, 0x1

    :converted
    add-int/2addr v0, p1
    mul-int/lit8 v0, v0, 0x1f
    return v0
.end method
