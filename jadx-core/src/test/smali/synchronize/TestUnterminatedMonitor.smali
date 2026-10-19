.class public final Lsynchronize/TestUnterminatedMonitor;
.super Ljava/lang/Object;

.method public static lock(Ljava/lang/Object;)V
    .registers 1

    monitor-enter p0
.end method

.method public static conditionalLock(Ljava/lang/Object;Z)V
    .registers 2

    monitor-enter p0
    if-eqz p1, :missing_exit
    monitor-exit p0
    return-void

    :missing_exit
    return-void
.end method
