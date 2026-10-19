.class public final Ltrycatch/TestSplitProtectedRetry;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static run(II)I
    .registers 6
    const/4 v0, 0x0
    const/4 v2, 0x2
    if-eqz p0, :initial
    goto :resume

    :initial
    const/4 v1, 0x0
    invoke-static {v1, p1}, Ljadx/tests/integration/trycatch/TestSplitProtectedRetry;->probe(II)V
    goto :loop

    :resume
    :try_start
    const/4 v1, 0x2
    invoke-static {v1, p1}, Ljadx/tests/integration/trycatch/TestSplitProtectedRetry;->probe(II)V
    goto :join

    :loop
    const/4 v1, 0x1
    invoke-static {v1, p1}, Ljadx/tests/integration/trycatch/TestSplitProtectedRetry;->probe(II)V
    :join
    add-int/lit8 v0, v0, 0x1
    if-lt v0, v2, :loop
    return v0
    :try_end
    .catchall {:try_start .. :try_end} :catch

    :catch
    move-exception v3
    invoke-static {v3}, Ljadx/tests/integration/trycatch/TestSplitProtectedRetry;->retry(Ljava/lang/Throwable;)Z
    move-result v1
    if-nez v1, :loop
    throw v3
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSplitProtectedRetry;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSplitProtectedRetry;->check(Ljava/lang/Class;)V
    return-void
.end method
