.class public Lsynchronize/TestSynchronizedOuterCatchReturn;
.super Ljava/lang/Object;

.field private final lock:Ljava/lang/Object;

.method public constructor <init>()V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    iput-object v0, p0, Lsynchronize/TestSynchronizedOuterCatchReturn;->lock:Ljava/lang/Object;
    return-void
.end method

.method private static native work()V
.end method

.method private static native report(Ljava/lang/Throwable;)V
.end method

.method public run()V
    .registers 3

    :try_start_enter
    iget-object v0, p0, Lsynchronize/TestSynchronizedOuterCatchReturn;->lock:Ljava/lang/Object;
    monitor-enter v0
    :try_end_enter
    .catchall {:try_start_enter .. :try_end_enter} :catch_outer

    :try_start_body
    invoke-static {}, Lsynchronize/TestSynchronizedOuterCatchReturn;->work()V
    monitor-exit v0
    :try_end_body
    .catchall {:try_start_body .. :try_end_body} :catch_monitor

    goto :return

    :catch_monitor
    move-exception v1
    monitor-exit v0
    :try_end_monitor
    .catchall {:try_start_body .. :try_end_monitor} :catch_monitor

    :try_start_rethrow
    throw v1
    :try_end_rethrow2
    .catchall {:try_start_rethrow .. :try_end_rethrow2} :catch_outer

    :catch_outer
    move-exception v0
    invoke-static {v0}, Lsynchronize/TestSynchronizedOuterCatchReturn;->report(Ljava/lang/Throwable;)V

    :return
    return-void
.end method
