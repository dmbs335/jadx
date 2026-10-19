.class public Ltrycatch/TestTryLeaveResultUse;
.super Ljava/lang/Object;

.method public static run(IZ)I
    .registers 4

    :try_start_produce
    invoke-static {p0}, Ljadx/tests/integration/trycatch/TestTryLeaveResultUse;->provide(I)Ljava/lang/Iterable;
    move-result-object v0
    :try_end_produce
    .catch Ljava/lang/IllegalArgumentException; {:try_start_produce .. :try_end_produce} :catch_produce

    invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :empty
    const/4 v0, 0x7
    return v0

    :empty
    const/4 v0, 0x0
    return v0

    :catch_produce
    move-exception v0
    if-eqz p1, :rethrow
    const/16 v0, 0x63
    return v0

    :rethrow
    throw v0
.end method

.method public static check()V
    .registers 3

    const/4 v0, 0x0
    const/4 v1, 0x1
    invoke-static {v0, v1}, Ltrycatch/TestTryLeaveResultUse;->run(IZ)I
    move-result v2
    const/4 v0, 0x7
    if-ne v2, v0, :fail

    const/4 v0, 0x2
    invoke-static {v0, v1}, Ltrycatch/TestTryLeaveResultUse;->run(IZ)I
    move-result v2
    const/16 v0, 0x63
    if-ne v2, v0, :fail

    const/4 v0, 0x1
    :try_start_after
    invoke-static {v0, v1}, Ltrycatch/TestTryLeaveResultUse;->run(IZ)I
    :try_end_after
    .catch Ljava/lang/IllegalArgumentException; {:try_start_after .. :try_end_after} :expected
    goto :fail

    :expected
    move-exception v0
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
