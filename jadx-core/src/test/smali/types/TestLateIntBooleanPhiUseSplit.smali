.class public Ltypes/TestLateIntBooleanPhiUseSplit;
.super Ljava/lang/Object;

.method private static acceptBoolean(Z)V
    .registers 1
    return-void
.end method

.method public static test([Ljava/lang/Object;)V
    .registers 3

    const/4 v0, 0x1

    :loop
    if-ltz v0, :end
    aget-object v1, p0, v0
    invoke-static {v0}, Ltypes/TestLateIntBooleanPhiUseSplit;->acceptBoolean(Z)V
    add-int/lit8 v0, v0, -0x1
    goto :loop

    :end
    return-void
.end method
