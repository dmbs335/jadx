.class public Ltrycatch/TestSingleSiteCatchHandlerScope;
.super Ljava/lang/Object;

.method public static run(ZZ)I
    .locals 2

    :try_probe_start
    invoke-static {p0}, Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope;->probe(Z)V
    :try_probe_end
    .catch Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope$Timeout; {:try_probe_start .. :try_probe_end} :timeout
    .catchall {:try_probe_start .. :try_probe_end} :outer_all
    const/4 v0, 0x0
    return v0

    :timeout
    move-exception v1
    :try_inner_start
    invoke-static {p1}, Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope;->inner(Z)V
    :try_inner_end
    .catchall {:try_inner_start .. :try_inner_end} :inner_all
    const/4 v0, 0x1
    return v0

    :inner_all
    move-exception v1
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope;->innerCleanup()V
    throw v1

    :outer_all
    move-exception v1
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope;->outerCleanup()V
    throw v1
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSingleSiteCatchHandlerScope;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSingleSiteCatchHandlerScope;->check(Ljava/lang/Class;)V
    return-void
.end method
