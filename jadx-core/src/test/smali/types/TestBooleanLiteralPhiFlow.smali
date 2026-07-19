.class public Ltypes/TestBooleanLiteralPhiFlow;
.super Ljava/lang/Object;

.method public static test(II)Z
    .registers 5

    and-int/lit16 v0, p0, 0x380
    const/16 v1, 0x100
    if-ne v0, v1, :first_false
    const/4 v0, 0x1
    goto :first_merge

    :first_false
    const/4 v0, 0x0

    :first_merge
    and-int/lit8 v1, p1, 0x70
    const/16 v2, 0x20
    if-ne v1, v2, :second_false
    const/4 v1, 0x1
    goto :second_merge

    :second_false
    const/4 v1, 0x0

    :second_merge
    or-int/2addr v0, v1
    return v0
.end method
