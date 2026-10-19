.class public Lswitches/TestProtectedCaseNormalCleanup;
.super Ljava/lang/Object;

.method public static run(IZ)I
    .locals 1

    packed-switch p0, :switch_data
    const/4 v0, 0x0
    return v0

    :case_zero
    :try_start
    invoke-static {p1}, Ljadx/tests/integration/switches/TestProtectedCaseNormalCleanup;->probe(Z)V
    :try_end
    .catch Ljava/lang/IllegalStateException; {:try_start .. :try_end} :catch_fail
    invoke-static {}, Ljadx/tests/integration/switches/TestProtectedCaseNormalCleanup;->cleanup()V
    const/16 v0, 0xa
    return v0

    :catch_fail
    move-exception v0
    invoke-static {}, Ljadx/tests/integration/switches/TestProtectedCaseNormalCleanup;->cleanup()V
    const/4 v0, -0x1
    return v0

    :case_one
    const/16 v0, 0x14
    return v0

    :switch_data
    .packed-switch 0x0
        :case_zero
        :case_one
    .end packed-switch
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Lswitches/TestProtectedCaseNormalCleanup;
    invoke-static {v0}, Ljadx/tests/integration/switches/TestProtectedCaseNormalCleanup;->check(Ljava/lang/Class;)V
    return-void
.end method
