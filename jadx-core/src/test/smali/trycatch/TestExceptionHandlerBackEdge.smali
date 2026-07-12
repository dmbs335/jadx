.class public Ltrycatch/TestExceptionHandlerBackEdge;
.super Ljava/lang/Object;

.method public synchronized waitUntil(Z)V
    .locals 1

    :loop
    :outer_try_start
    invoke-static {}, Ltrycatch/TestExceptionHandlerBackEdge;->check()Z
    move-result v0
    :outer_try_end
    .catchall {:outer_try_start .. :outer_try_end} :outer_catch

    if-nez v0, :done

    :try_start
    invoke-virtual {p0}, Ljava/lang/Object;->wait()V
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

    goto :loop

    :catch_all
    move-exception v0
    throw v0

    :outer_catch
    move-exception v0
    throw v0

    :done
    return-void
.end method

.method private static check()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method
