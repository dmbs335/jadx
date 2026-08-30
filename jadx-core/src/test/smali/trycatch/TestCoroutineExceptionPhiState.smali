.class public Ltrycatch/TestCoroutineExceptionPhiState;
.super Ljava/lang/Object;

.field private label:I
.field private saved:Ljava/lang/Object;

.method public invokeSuspend(Ljava/io/File;)V
    .locals 2
	.annotation system Ldalvik/annotation/Throws;
		value = {
			Ljava/lang/Throwable;
		}
	.end annotation

    iget v0, p0, Ltrycatch/TestCoroutineExceptionPhiState;->label:I
    if-nez v0, :resume

    new-instance v0, Ljava/io/FileInputStream;
    invoke-direct {v0, p1}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    iput-object v0, p0, Ltrycatch/TestCoroutineExceptionPhiState;->saved:Ljava/lang/Object;

    :initial_try_start
    invoke-static {v0}, Ltrycatch/TestCoroutineExceptionPhiState;->use(Ljava/io/Closeable;)V
    :initial_try_end
    .catchall {:initial_try_start .. :initial_try_end} :handler
    goto :close

    :resume
    iget-object v0, p0, Ltrycatch/TestCoroutineExceptionPhiState;->saved:Ljava/lang/Object;
    check-cast v0, Ljava/io/Closeable;

    :resume_try_start
    invoke-static {v0}, Ltrycatch/TestCoroutineExceptionPhiState;->use(Ljava/io/Closeable;)V
    :resume_try_end
    .catchall {:resume_try_start .. :resume_try_end} :handler

    :close
    invoke-static {v0}, Ltrycatch/TestCoroutineExceptionPhiState;->close(Ljava/io/Closeable;)V
    return-void

    :handler
    move-exception v1
    invoke-static {v0}, Ltrycatch/TestCoroutineExceptionPhiState;->close(Ljava/io/Closeable;)V
    throw v1
.end method

.method private static close(Ljava/io/Closeable;)V
    .locals 0

    return-void
.end method

.method private static use(Ljava/io/Closeable;)V
    .locals 0

    return-void
.end method
