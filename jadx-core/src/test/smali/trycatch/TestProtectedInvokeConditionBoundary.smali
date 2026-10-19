.class public Ltrycatch/TestProtectedInvokeConditionBoundary;
.super Ljava/lang/Object;

.method public static run(ZZ)I
    .locals 3

    :try_start
    invoke-static {p0}, Ljadx/tests/integration/trycatch/TestProtectedInvokeConditionBoundary;->protectedValue(Z)I
    move-result v0
    :try_end
    .catchall {:try_start .. :try_end} :all

    invoke-static {p1}, Ljadx/tests/integration/trycatch/TestProtectedInvokeConditionBoundary;->unprotectedValue(Z)I
    move-result v1
    if-ne v0, v1, :different
    const/4 v2, 0x1
    return v2

    :different
    const/4 v2, 0x0
    return v2

    :all
    move-exception v2
    const/4 v2, 0x2
    return v2
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestProtectedInvokeConditionBoundary;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestProtectedInvokeConditionBoundary;->check(Ljava/lang/Class;)V
    return-void
.end method
