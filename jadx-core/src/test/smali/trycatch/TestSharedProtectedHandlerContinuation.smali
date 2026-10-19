.class public Ltrycatch/TestSharedProtectedHandlerContinuation;
.super Ljava/lang/Object;

.method public static run(IZZ)I
    .locals 2

    packed-switch p0, :switch_data
    const/4 v0, -0x1
    return v0

    :case_zero
    :try_zero_start
    invoke-static {p1}, Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation;->probe(Z)V
    :try_zero_end
    .catch Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation$Timeout; {:try_zero_start .. :try_zero_end} :timeout_zero
    goto :normal

    :case_one
    :try_one_start
    invoke-static {p1}, Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation;->probe(Z)V
    :try_one_end
    .catch Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation$Timeout; {:try_one_start .. :try_one_end} :timeout_one
    goto :normal

    :timeout_zero
    move-exception v1
    goto :shared_work

    :timeout_one
    move-exception v1
    goto :shared_work

    :shared_work
    :try_work_start
    invoke-static {p2}, Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation;->work(Z)V
    :try_work_end
    .catchall {:try_work_start .. :try_work_end} :work_all
    const/4 v0, 0x1
    return v0

    :work_all
    move-exception v1
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation;->workCleanup()V
    throw v1

    :normal
    const/4 v0, 0x0
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
    const-class v0, Ltrycatch/TestSharedProtectedHandlerContinuation;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSharedProtectedHandlerContinuation;->check(Ljava/lang/Class;)V
    return-void
.end method
