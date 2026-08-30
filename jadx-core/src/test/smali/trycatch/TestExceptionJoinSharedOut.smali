.class public Ltrycatch/TestExceptionJoinSharedOut;
.super Ljava/lang/Object;

.method private static mayThrow()V
    .locals 0

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    return-void
.end method

.method private static use()V
    .locals 0
    return-void
.end method

.method public static run(ZZ)V
    .locals 1

    :try_start
    invoke-static {}, Ltrycatch/TestExceptionJoinSharedOut;->mayThrow()V
    :try_end
    .catch Ljava/io/IOException; {:try_start .. :try_end} :handler

    if-eqz p0, :join

    :try_close_start
    invoke-static {}, Ltrycatch/TestExceptionJoinSharedOut;->mayThrow()V
    :try_close_end
    .catch Ljava/io/IOException; {:try_close_start .. :try_close_end} :join
    goto :join

    :handler
    move-exception v0

    :join
    if-eqz p1, :out
    invoke-static {}, Ltrycatch/TestExceptionJoinSharedOut;->use()V

    :out
    return-void
.end method
