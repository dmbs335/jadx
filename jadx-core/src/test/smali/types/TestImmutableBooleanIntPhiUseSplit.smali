.class public Ltypes/TestImmutableBooleanIntPhiUseSplit;
.super Ljava/lang/Object;

.method public static test(Z[Ljava/lang/Object;[Z)Ljava/lang/Object;
    .registers 6

    if-eqz p0, :second_one
    const/4 v0, 0x1
    goto :merge

    :second_one
    const/4 v0, 0x1

    :merge
    aget-object v1, p1, v0
    const/4 v2, 0x0
    aput-boolean v0, p2, v2
    return-object v1
.end method
