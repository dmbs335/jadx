.class public final Ltrycatch/TestSequentialParseExceptionCatch;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public declared-synchronized run(I)I
    .registers 4
    monitor-enter p0

    :local_start
    const/4 v0, 0x0
    invoke-static {v0, p1}, Ljadx/tests/integration/trycatch/TestSequentialParseExceptionCatch;->parse(II)I
    move-result p1
    :local_end
    .catch Ljava/text/ParseException; {:local_start .. :local_end} :local_failure
    .catchall {:local_start .. :local_end} :catchall
    monitor-exit p0
    return p1

    :catchall
    move-exception p1
    goto :cleanup_exit

    :local_failure
    :en_start
    const/4 v0, 0x1
    invoke-static {v0, p1}, Ljadx/tests/integration/trycatch/TestSequentialParseExceptionCatch;->parse(II)I
    move-result p1
    :en_end
    .catch Ljava/text/ParseException; {:en_start .. :en_end} :en_failure
    .catchall {:en_start .. :en_end} :catchall
    monitor-exit p0
    return p1

    :en_failure
    :iso_start
    const/4 v0, 0x2
    invoke-static {v0, p1}, Ljadx/tests/integration/trycatch/TestSequentialParseExceptionCatch;->parse(II)I
    move-result p1
    :iso_end
    .catch Ljava/text/ParseException; {:iso_start .. :iso_end} :iso_failure
    .catchall {:iso_start .. :iso_end} :catchall
    monitor-exit p0
    return p1

    :iso_failure
    move-exception v0
    :fail_start
    new-instance v1, Ljava/lang/IllegalArgumentException;
    const-string p1, "parse"
    invoke-direct {v1, p1, v0}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V
    throw v1

    :cleanup_exit
    monitor-exit p0
    :fail_end
    .catchall {:fail_start .. :fail_end} :catchall
    throw p1
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSequentialParseExceptionCatch;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSequentialParseExceptionCatch;->check(Ljava/lang/Class;)V
    return-void
.end method
