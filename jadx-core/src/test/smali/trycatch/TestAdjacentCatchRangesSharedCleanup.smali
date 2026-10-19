.class public Ltrycatch/TestAdjacentCatchRangesSharedCleanup;
.super Ljava/lang/Object;

.method public static run(I)I
    .locals 3
    const/4 v0, 0x0

    :loop
    if-ge v0, p0, :done

    :try_a_start
    invoke-static {}, Ljadx/tests/integration/trycatch/TestAdjacentCatchRangesSharedCleanup;->probeA()V
    :try_a_end
    .catchall {:try_a_start .. :try_a_end} :handler_a

    :try_b_start
    invoke-static {}, Ljadx/tests/integration/trycatch/TestAdjacentCatchRangesSharedCleanup;->probeB()V
    :try_b_end
    .catchall {:try_b_start .. :try_b_end} :handler_b

    add-int/lit8 v0, v0, 0x1
    goto :loop

    :handler_a
    move-exception v1
    goto :shared_cleanup

    :handler_b
    move-exception v1
    goto :shared_cleanup

    :shared_cleanup
    invoke-static {}, Ljadx/tests/integration/trycatch/TestAdjacentCatchRangesSharedCleanup;->active()Z
    move-result v2
    if-nez v2, :cleanup
    const/4 v2, 0x0

    :cleanup
    invoke-static {v2}, Ljadx/tests/integration/trycatch/TestAdjacentCatchRangesSharedCleanup;->cleanup(Z)V
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
    const-class v0, Ltrycatch/TestAdjacentCatchRangesSharedCleanup;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestAdjacentCatchRangesSharedCleanup;->check(Ljava/lang/Class;)V
    return-void
.end method
