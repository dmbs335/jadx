.class public Lconditions/TestSharedTerminalBranches;
.super Ljava/lang/Object;

.method public static choose(II)I
    .locals 1

    if-eqz p0, :second
    if-eqz p1, :first
    if-ge p1, p0, :second

    :first
    const/4 v0, 0x1
    return v0

    :second
    const/4 v0, 0x2
    return v0
.end method
