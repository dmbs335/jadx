.class public Lsynchronize/TestMonitorExitSameBlock;
.super Ljava/lang/Object;

.field public static trace:I

.method public static run()V
    .registers 2

    const-class v0, Lsynchronize/TestMonitorExitSameBlock;
    monitor-enter v0
    const/4 v1, 0x1
    sput v1, Lsynchronize/TestMonitorExitSameBlock;->trace:I
    monitor-exit v0
    const/4 v1, 0x2
    sput v1, Lsynchronize/TestMonitorExitSameBlock;->trace:I
    return-void
.end method

.method public static check()V
    .registers 2

    invoke-static {}, Lsynchronize/TestMonitorExitSameBlock;->run()V
    sget v0, Lsynchronize/TestMonitorExitSameBlock;->trace:I
    const/4 v1, 0x2
    if-eq v0, v1, :ok
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0

    :ok
    return-void
.end method
