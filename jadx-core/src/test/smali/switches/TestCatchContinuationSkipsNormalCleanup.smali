.class public Lswitches/TestCatchContinuationSkipsNormalCleanup;
.super Ljava/lang/Object;

.method public static run(IZZ)I
    .locals 1

    packed-switch p0, :switch_data
    const/4 v0, -0x1
    return v0

    :case_zero
    :try_start
    invoke-static {p1}, Ljadx/tests/integration/switches/TestCatchContinuationSkipsNormalCleanup;->probe(Z)V
    :try_end
    .catch Ljava/lang/IllegalStateException; {:try_start .. :try_end} :catch_fail
    invoke-static {}, Ljadx/tests/integration/switches/TestCatchContinuationSkipsNormalCleanup;->normalCleanup()V
    const/16 v0, 0xa
    return v0

    :catch_fail
    move-exception v0
    :catch_action_start
    invoke-static {}, Ljadx/tests/integration/switches/TestCatchContinuationSkipsNormalCleanup;->catchAction()V
    :catch_action_end
    .catchall {:catch_action_start .. :catch_action_end} :catch_action_failed
    if-eqz p2, :shared_cleanup
    const/16 v0, 0x63
    return v0

    :catch_action_failed
    move-exception v0
    throw v0

    :case_one
    goto :shared_cleanup

    :shared_cleanup
    invoke-static {}, Ljadx/tests/integration/switches/TestCatchContinuationSkipsNormalCleanup;->sharedCleanup()V
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
    const-class v0, Lswitches/TestCatchContinuationSkipsNormalCleanup;
    invoke-static {v0}, Ljadx/tests/integration/switches/TestCatchContinuationSkipsNormalCleanup;->check(Ljava/lang/Class;)V
    return-void
.end method
