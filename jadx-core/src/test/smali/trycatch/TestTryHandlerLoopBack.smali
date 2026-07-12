.class public Ltrycatch/TestTryHandlerLoopBack;
.super Ljava/lang/Object;

.method private static mayThrow()V
    .locals 0

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    return-void
.end method

.method public static run(Z)I
    .locals 2

    const/4 v0, 0x0

    :loop
    if-eqz p0, :done

    :try_start
    invoke-static {}, Ltrycatch/TestTryHandlerLoopBack;->mayThrow()V
    :try_end
    .catchall {:try_start .. :try_end} :handler

    const/4 p0, 0x0
    goto :loop

    :handler
    move-exception v1
    add-int/lit8 v0, v0, 0x1
    const/4 p0, 0x0
    goto :loop

    :done
    return v0
.end method
