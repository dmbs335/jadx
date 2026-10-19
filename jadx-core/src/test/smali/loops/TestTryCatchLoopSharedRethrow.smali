.class public final Lloops/TestTryCatchLoopSharedRethrow;
.super Ljava/lang/Object;

.method private static native request()V
.end method

.method private static native shouldRetry(Ljava/lang/Throwable;)Z
.end method

.method public static run(I)V
    .registers 4

    const/4 v0, 0x0

    :loop
    :try_start
    invoke-static {}, Lloops/TestTryCatchLoopSharedRethrow;->request()V
    :try_end
    .catchall {:try_start .. :try_end} :catch
    return-void

    :catch
    move-exception v1
    if-ge v0, p0, :rethrow
    invoke-static {v1}, Lloops/TestTryCatchLoopSharedRethrow;->shouldRetry(Ljava/lang/Throwable;)Z
    move-result v2
    if-eqz v2, :rethrow
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :rethrow
    throw v1
.end method
