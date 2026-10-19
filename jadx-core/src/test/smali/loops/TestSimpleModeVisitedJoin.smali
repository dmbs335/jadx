.class public Lloops/TestSimpleModeVisitedJoin;
.super Ljava/lang/Object;

.method public static test(ZZI)I
    .registers 4

    const/4 v0, 0x0
    if-eqz p0, :second_path
    const/4 v0, 0x1
    goto :join

    :second_path
    if-eqz p1, :loop
    add-int/lit8 v0, v0, 0x2

    :join
    invoke-static {v0}, Lloops/TestSimpleModeVisitedJoin;->record(I)V
    return v0

    :loop
    add-int/lit8 v0, v0, 0x1
    if-lt v0, p2, :loop
    goto :join
.end method

.method public static record(I)V
    .registers 1
    return-void
.end method
