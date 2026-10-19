.class public Ltrycatch/TestSharedConditionalCleanupThrowTail;
.super Ljava/lang/Object;

.method public static run(I)I
    .locals 3
    const/4 v0, 0x0

    :loop
    if-ge v0, p0, :done
	if-eqz v0, :probe_first

	:try_b_start
	invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedConditionalCleanupThrowTail;->probeB()V
	:try_b_end
	.catchall {:try_b_start .. :try_b_end} :handler_b
	goto :after_probe

	:probe_first
    :try_a_start
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedConditionalCleanupThrowTail;->probeA()V
    :try_a_end
    .catchall {:try_a_start .. :try_a_end} :handler_a

	:after_probe
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :handler_a
    move-exception v1
    goto :shared_cleanup

    :handler_b
    move-exception v1
    goto :shared_cleanup

    :shared_cleanup
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedConditionalCleanupThrowTail;->active()Z
    move-result v2
    if-nez v2, :cleanup
    const/4 v2, 0x0

    :cleanup
    invoke-static {v2}, Ljadx/tests/integration/trycatch/TestSharedConditionalCleanupThrowTail;->cleanup(Z)V
    goto :rethrow

    :rethrow
    throw v1

    :done
    return v0
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSharedConditionalCleanupThrowTail;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSharedConditionalCleanupThrowTail;->check(Ljava/lang/Class;)V
    return-void
.end method
