.class public Ltypes/TestBooleanIntArrayPhi;
.super Ljava/lang/Object;

.method public static test([IZI)V
    .registers 4

    if-eqz p1, :zero
    move v0, p1
    goto :store

    :zero
    const/4 v0, 0x0

    :store
    aput v0, p0, p2
    return-void
.end method
