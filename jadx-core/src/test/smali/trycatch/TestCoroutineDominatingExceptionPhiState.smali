.class public Ltrycatch/TestCoroutineDominatingExceptionPhiState;
.super Ljava/lang/Object;

.field private label:I
.field private saved:Ljava/lang/Object;

.method public invokeSuspend(Ljava/io/File;)V
    .locals 4
	.annotation system Ldalvik/annotation/Throws;
		value = {
			Ljava/lang/Throwable;
		}
	.end annotation

    iget v1, p0, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->label:I
    if-nez v1, :resume

    new-instance v3, Ljava/io/FileInputStream;
    invoke-direct {v3, p1}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    iput-object v3, p0, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->saved:Ljava/lang/Object;
    goto :body

    :resume
    iget-object v3, p0, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->saved:Ljava/lang/Object;
    check-cast v3, Ljava/io/Closeable;

    :body
    move-object v1, v3
    :inner_try_start
    invoke-static {v3}, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->use(Ljava/io/Closeable;)V
    :inner_try_end
    .catchall {:inner_try_start .. :inner_try_end} :inner_handler

    move-object v1, v3
    :normal_cleanup_try_start
    invoke-static {}, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->cleanup()V
    :normal_cleanup_try_end
    .catchall {:normal_cleanup_try_start .. :normal_cleanup_try_end} :outer_handler
    invoke-static {v1}, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->close(Ljava/io/Closeable;)V
    return-void

    :inner_handler
    move-exception v2
    move-object v1, v3
    :catch_cleanup_try_start
    invoke-static {}, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->cleanup()V
    :catch_cleanup_try_end
    .catchall {:catch_cleanup_try_start .. :catch_cleanup_try_end} :outer_handler
    throw v2

    :outer_handler
    move-exception v2
    invoke-static {v1}, Ltrycatch/TestCoroutineDominatingExceptionPhiState;->close(Ljava/io/Closeable;)V
    throw v2
.end method

.method private static cleanup()V
    .locals 0

    return-void
.end method

.method private static close(Ljava/io/Closeable;)V
    .locals 0

    return-void
.end method

.method private static use(Ljava/io/Closeable;)V
    .locals 0

    return-void
.end method
