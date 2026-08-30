.class public Lconditions/TestPhiAwareLoopBranch;
.super Ljava/lang/Object;

.method public static test([IIZ)I
    .registers 9

    const/4 v0, 0x0
    const/4 v1, 0x0
    array-length v5, p0

    :loop
    if-ge v0, v5, :end
    aget v2, p0, v0
    rem-int/lit8 v3, v0, 0x2
    if-eqz v3, :non_focusable

    if-gt v2, p1, :update
    if-ne v2, p1, :continue
    if-le v0, v1, :tie_false
    const/4 v4, 0x1
    goto :tie_join

    :tie_false
    const/4 v4, 0x0

    :tie_join
    if-ne p2, v4, :continue
    goto :update

    :non_focusable
    if-le v2, v1, :continue

    :update
    if-eqz v3, :update_non_focusable
    move p1, v2
    move v1, v0
    goto :continue

    :update_non_focusable
    add-int/2addr p1, v2
    move v1, v0

    :continue
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :end
    return p1
.end method
