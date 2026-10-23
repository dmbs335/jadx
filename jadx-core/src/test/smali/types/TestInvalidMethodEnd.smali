.class public Ltypes/TestInvalidMethodEnd;
.super Ljava/lang/Object;

.method public static noReturn()I
    .locals 1
    const/4 v0, 0x1
.end method

.method public static conditionalFallOff(Z)I
    .locals 1
    if-eqz p0, :end
    const/4 v0, 0x1
    return v0
    :end
    nop
.end method

.method public static validReturn()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static infiniteLoop()V
    .locals 0
    :loop
    nop
    goto :loop
.end method

.method public static unreachableTail()I
    .locals 1
    const/4 v0, 0x1
    return v0
    const/4 v0, 0x2
.end method
