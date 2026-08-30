.class public Lloops/TestLoopPhiCarryBeforeContinue;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;
    .registers 7

    move-object v0, p0

    :loop
    if-eqz p2, :return
    if-nez p3, :carry

    monitor-enter p0
    :try_start
    if-eqz p1, :locked_return
    monitor-exit p0
    goto :carry

    :locked_return
    monitor-exit p0
    return-object v0
    :try_end
    .catchall {:try_start .. :try_end} :catchall

    :catchall
    move-exception v1
    monitor-exit p0
    throw v1

    :carry
    move-object v0, p1
    goto :loop

    :return
    return-object v0
.end method
