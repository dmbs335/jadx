.class public Ltrycatch/TestSharedCleanupTailDedup;
.super Ljava/lang/Object;

.method public static run(II)I
    .locals 3

    if-eqz p0, :second

    :first_try_start
    invoke-static {p0, p1}, Ljadx/tests/integration/trycatch/TestSharedCleanupTailDedup;->probe(II)V
    :first_try_end
    .catchall {:first_try_start .. :first_try_end} :first_catch
    const/4 v2, 0x1
    return v2

    :first_catch
    move-exception v0
    .local v0, "th":Ljava/lang/Throwable;
    move-object v1, v0
    goto :shared_cleanup

    :second
    :second_try_start
    invoke-static {p0, p1}, Ljadx/tests/integration/trycatch/TestSharedCleanupTailDedup;->probe(II)V
    :second_try_end
    .catchall {:second_try_start .. :second_try_end} :second_catch
    const/4 v2, 0x2
    return v2

    :second_catch
    move-exception v0
    .local v0, "th":Ljava/lang/Throwable;
    move-object v1, v0

    :shared_cleanup
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedCleanupTailDedup;->cleanup()V
    throw v1
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSharedCleanupTailDedup;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSharedCleanupTailDedup;->check(Ljava/lang/Class;)V
    return-void
.end method
