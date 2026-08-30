.class public Lsynchronize/TestSynchronizedLoopPostMonitorWork;
.super Ljava/lang/Object;

.field private static head:Lsynchronize/TestSynchronizedLoopPostMonitorWork;

.method private static native next()Lsynchronize/TestSynchronizedLoopPostMonitorWork;
.end method

.method private native work()V
.end method

.method public static run()V
    .registers 4

    :loop
    const-class v0, Lsynchronize/TestSynchronizedLoopPostMonitorWork;
    monitor-enter v0

    :try_start
    invoke-static {}, Lsynchronize/TestSynchronizedLoopPostMonitorWork;->next()Lsynchronize/TestSynchronizedLoopPostMonitorWork;
    move-result-object v1
    if-nez v1, :not_null
    monitor-exit v0
    goto :loop

    :not_null
    sget-object v2, Lsynchronize/TestSynchronizedLoopPostMonitorWork;->head:Lsynchronize/TestSynchronizedLoopPostMonitorWork;
    if-ne v1, v2, :work
    const/4 v2, 0x0
    sput-object v2, Lsynchronize/TestSynchronizedLoopPostMonitorWork;->head:Lsynchronize/TestSynchronizedLoopPostMonitorWork;
    monitor-exit v0
    return-void

    :work
    monitor-exit v0
    :try_end
    invoke-virtual {v1}, Lsynchronize/TestSynchronizedLoopPostMonitorWork;->work()V
    goto :loop

    :catchall
    move-exception v1
    monitor-exit v0
    throw v1

    .catchall {:try_start .. :try_end} :catchall
.end method
