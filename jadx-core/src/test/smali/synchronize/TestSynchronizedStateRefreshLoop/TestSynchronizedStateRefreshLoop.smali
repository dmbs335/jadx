.class public Lsynchronize/TestSynchronizedStateRefreshLoop;
.super Ljava/lang/Object;

.field private final lock:Ljava/lang/Object;
.field private state:Lsynchronize/TestSynchronizedStateRefreshLoop$State;

.method public constructor <init>()V
    .registers 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    iput-object v0, p0, Lsynchronize/TestSynchronizedStateRefreshLoop;->lock:Ljava/lang/Object;
    new-instance v0, Lsynchronize/TestSynchronizedStateRefreshLoop$State;
    invoke-direct {v0}, Lsynchronize/TestSynchronizedStateRefreshLoop$State;-><init>()V
    iput-object v0, p0, Lsynchronize/TestSynchronizedStateRefreshLoop;->state:Lsynchronize/TestSynchronizedStateRefreshLoop$State;
    return-void
.end method

.method private static native create()Ljava/lang/Object;
.end method

.method private static native failure()Ljava/lang/Object;
.end method

.method private static native drain()V
.end method

.method public run()Ljava/lang/Object;
    .registers 6

    :outer_start
    iget-object v0, p0, Lsynchronize/TestSynchronizedStateRefreshLoop;->state:Lsynchronize/TestSynchronizedStateRefreshLoop$State;

    :loop
    iget-object v1, v0, Lsynchronize/TestSynchronizedStateRefreshLoop$State;->status:Ljava/lang/Object;
    if-nez v1, :failed

    iget-object v2, p0, Lsynchronize/TestSynchronizedStateRefreshLoop;->lock:Ljava/lang/Object;
    monitor-enter v2
    :outer_before_monitor
    .catchall {:outer_start .. :outer_before_monitor} :outer_catch

    :try_start
    iget-object v3, p0, Lsynchronize/TestSynchronizedStateRefreshLoop;->state:Lsynchronize/TestSynchronizedStateRefreshLoop$State;
    if-eq v0, v3, :create
    monitor-exit v2
    :try_end_refresh
    .catchall {:try_start .. :try_end_refresh} :catchall

    move-object v0, v3
    goto :loop

    :create
    invoke-static {}, Lsynchronize/TestSynchronizedStateRefreshLoop;->create()Ljava/lang/Object;
    move-result-object v4
    monitor-exit v2
    :try_end_create
    .catchall {:try_start .. :try_end_create} :catchall

    :outer_create_start
    invoke-static {}, Lsynchronize/TestSynchronizedStateRefreshLoop;->drain()V
    return-object v4
    :outer_create_end
    .catchall {:outer_create_start .. :outer_create_end} :outer_catch

    :catchall
    move-exception v4
    monitor-exit v2

    :outer_rethrow_start
    throw v4
    :outer_rethrow_end
    .catchall {:outer_rethrow_start .. :outer_rethrow_end} :outer_catch

    :failed
    :outer_failed_start
    invoke-static {}, Lsynchronize/TestSynchronizedStateRefreshLoop;->failure()Ljava/lang/Object;
    move-result-object v1
    invoke-static {}, Lsynchronize/TestSynchronizedStateRefreshLoop;->drain()V
    return-object v1
    :outer_failed_end
    .catchall {:outer_failed_start .. :outer_failed_end} :outer_catch

    :outer_catch
    move-exception v1
    invoke-static {}, Lsynchronize/TestSynchronizedStateRefreshLoop;->drain()V
    throw v1
.end method
