.class public Ltrycatch/TestSynchronizedHandlerLoop;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/Object;)V
    .locals 1

    monitor-enter p0

    :try_start
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    :try_end

    monitor-exit p0
    return-void

    :catchall_main
    move-exception v0

    :release
    :release_start
    monitor-exit p0
    :release_end
    throw v0

    :catchall_release
    move-exception v0
    goto :release

    .catchall {:try_start .. :try_end} :catchall_main
    .catchall {:release_start .. :release_end} :catchall_release
.end method
