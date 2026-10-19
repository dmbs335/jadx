.class public Lswitches/TestSharedCoroutineCatchTerminalCleanup;
.super Ljava/lang/Object;

.method public static run(IZZ)I
    .locals 3

    packed-switch p0, :switch_data
    const/4 v0, -0x1
    return v0

    :case_zero
    :case_one
    :try_initial_start
    invoke-static {p1}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->probe(Z)V
    :try_initial_end
    .catch Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup$Timeout; {:try_initial_start .. :try_initial_end} :timeout
    .catchall {:try_initial_start .. :try_initial_end} :all
    goto :normal_cleanup

    :case_two
    :try_resume_start
    invoke-static {p1}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->probe(Z)V
    :try_resume_end
    .catch Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup$Timeout; {:try_resume_start .. :try_resume_end} :timeout
    .catchall {:try_resume_start .. :try_resume_end} :all
    goto :normal_cleanup

    :normal_cleanup
    invoke-static {}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->cleanup()V
    goto :return_unit

    :timeout
    move-exception v2
    :try_wait_start
    invoke-static {p2}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->waitForUp(Z)I
    move-result v0
    :try_wait_end
    .catchall {:try_wait_start .. :try_wait_end} :all
    const/4 v1, 0x1
    if-ne v0, v1, :shared_consume
    return v1

    :case_three
    goto :shared_consume

    :shared_consume
    :try_consume_start
    invoke-static {}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->consume()V
    :try_consume_end
    .catchall {:try_consume_start .. :try_consume_end} :consume_all
    invoke-static {}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->cleanup()V
    goto :return_unit

    :consume_all
    move-exception v2
    invoke-static {}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->consumeFailureCleanup()V
    throw v2

    :all
    move-exception v2
    invoke-static {}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->cleanup()V
    throw v2

    :return_unit
    const/4 v0, 0x0
    return v0

    :switch_data
    .packed-switch 0x0
        :case_zero
        :case_one
        :case_two
        :case_three
    .end packed-switch
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Lswitches/TestSharedCoroutineCatchTerminalCleanup;
    invoke-static {v0}, Ljadx/tests/integration/switches/TestSharedCoroutineCatchTerminalCleanup;->check(Ljava/lang/Class;)V
    return-void
.end method
