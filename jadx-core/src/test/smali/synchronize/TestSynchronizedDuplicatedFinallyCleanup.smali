.class public Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;
.super Ljava/lang/Object;

.field private final lock:Ljava/lang/Object;

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    iput-object v0, p0, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->lock:Ljava/lang/Object;
    return-void
.end method

.method private static native prepareA(Ljava/lang/Object;)V
.end method

.method private static native prepareB(Ljava/lang/Object;)V
.end method

.method private static native before(Ljava/lang/Object;)V
.end method

.method private static native use(Ljava/lang/Object;)V
.end method

.method private static native release(Ljava/lang/Object;)V
.end method

.method public run(Ljava/lang/Object;ZZ)V
    .registers 8

    const/4 v0, 0x0

    :try_start_enter
    iget-object v1, p0, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->lock:Ljava/lang/Object;
    monitor-enter v1
    :try_end_enter
    .catchall {:try_start_enter .. :try_end_enter} :catchall_outer

    :try_start_before
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->before(Ljava/lang/Object;)V
    :try_end_before
    .catchall {:try_start_before .. :try_end_before} :catchall_no_release

    if-eqz p1, :null_resource
    if-eqz p2, :cleanup_b
    if-eqz p3, :cleanup_a

    :try_start_use
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->use(Ljava/lang/Object;)V
    monitor-exit v1
    :try_end_use
    .catchall {:try_start_use .. :try_end_use} :catchall_no_release

    return-void

    :cleanup_a
    :try_start_cleanup_a
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->prepareA(Ljava/lang/Object;)V
    monitor-exit v1
    :try_end_cleanup_a
    .catchall {:try_start_cleanup_a .. :try_end_cleanup_a} :catchall_release

    if-eqz p1, :return_a
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->release(Ljava/lang/Object;)V
    :return_a
    return-void

    :cleanup_b
    :try_start_cleanup_b
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->prepareB(Ljava/lang/Object;)V
    monitor-exit v1
    :try_end_cleanup_b
    .catchall {:try_start_cleanup_b .. :try_end_cleanup_b} :catchall_release

    if-eqz p1, :return_b
    invoke-static {p1}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->release(Ljava/lang/Object;)V
    :return_b
    return-void

    :null_resource
    :try_start_null
    monitor-exit v1
    :try_end_null
    .catchall {:try_start_null .. :try_end_null} :catchall_no_release

    return-void

    :catchall_release
    move-exception v2
    move-object v0, p1
    move-object p1, v2
    goto :monitor_rethrow

    :catchall_no_release
    move-exception p1

    :monitor_rethrow
    :try_start_monitor_rethrow
    monitor-exit v1
    :try_end_monitor_rethrow
    .catchall {:try_start_monitor_rethrow .. :try_end_monitor_rethrow} :catchall_no_release

    :try_start_rethrow
    throw p1
    :try_end_rethrow
    .catchall {:try_start_rethrow .. :try_end_rethrow} :catchall_outer

    :catchall_outer
    move-exception v3
    if-eqz v0, :outer_throw
    invoke-static {v0}, Lsynchronize/TestSynchronizedDuplicatedFinallyCleanup;->release(Ljava/lang/Object;)V
    :outer_throw
    throw v3
.end method
