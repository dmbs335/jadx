.class public Ltrycatch/TestSequentialExecutorLoop;
.super Ljava/lang/Object;

.field private static queue:Ljava/util/Deque;
.field private static state:I

.method public static test()V
    .locals 5

    const/4 v0, 0x0
    move v1, v0

    :loop
    :try_outer_start
    sget-object v2, Ltrycatch/TestSequentialExecutorLoop;->queue:Ljava/util/Deque;
    monitor-enter v2
    :try_outer_end
    .catchall {:try_outer_start .. :try_outer_end} :outer_catch

    if-nez v0, :poll

    :try_sync_start
    sget v3, Ltrycatch/TestSequentialExecutorLoop;->state:I
    const/4 v4, 0x1
    if-ne v3, v4, :set_running
    monitor-exit v2
    :try_sync_early_end
    .catchall {:try_sync_start .. :try_sync_early_end} :sync_catch

    if-eqz v1, :return
    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V
    goto :return

    :set_running
    sput v4, Ltrycatch/TestSequentialExecutorLoop;->state:I
    const/4 v0, 0x1

    :poll
    sget-object v3, Ltrycatch/TestSequentialExecutorLoop;->queue:Ljava/util/Deque;
    invoke-interface {v3}, Ljava/util/Deque;->poll()Ljava/lang/Object;
    move-result-object v3
    check-cast v3, Ljava/lang/Runnable;
    if-nez v3, :run_task

    const/4 v4, 0x0
    sput v4, Ltrycatch/TestSequentialExecutorLoop;->state:I
    monitor-exit v2
    :try_sync_end
    .catchall {:try_sync_start .. :try_sync_end} :sync_catch

    if-eqz v1, :return
    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Thread;->interrupt()V

    :return
    return-void

    :run_task
    monitor-exit v2
    :try_sync_task_end
    .catchall {:try_sync_start .. :try_sync_task_end} :sync_catch

    :try_task_start
    invoke-static {}, Ljava/lang/Thread;->interrupted()Z
    move-result v2
    or-int/2addr v1, v2
    invoke-interface {v3}, Ljava/lang/Runnable;->run()V
    :try_task_end
    .catch Ljava/lang/RuntimeException; {:try_task_start .. :try_task_end} :task_catch
    .catchall {:try_task_start .. :try_task_end} :outer_catch
    goto :loop

    :task_catch
    move-exception v2
    :try_log_start
    invoke-virtual {v2}, Ljava/lang/RuntimeException;->printStackTrace()V
    :try_log_end
    .catchall {:try_log_start .. :try_log_end} :outer_catch
    goto :loop

    :sync_catch
    move-exception v0
    :try_sync_throw_start
    monitor-exit v2
    :try_sync_throw_end
    .catchall {:try_sync_throw_start .. :try_sync_throw_end} :sync_catch
    :try_rethrow_start
    throw v0
    :try_rethrow_end
    .catchall {:try_rethrow_start .. :try_rethrow_end} :outer_catch

    :outer_catch
    move-exception v0
    if-eqz v1, :throw
    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;
    move-result-object v1
    invoke-virtual {v1}, Ljava/lang/Thread;->interrupt()V

    :throw
    throw v0
.end method
