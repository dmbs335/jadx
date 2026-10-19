.class public Ltrycatch/TestProtectedConditionTerminal;
.super Ljava/lang/Object;

.method public static run(I)I
    .registers 3

    :try_start_parse
    invoke-static {p0}, Ljadx/tests/integration/trycatch/TestProtectedConditionTerminal;->parse(I)Ljava/lang/Integer;
    move-result-object v0
    :try_end_parse
    .catch Ljava/lang/IllegalArgumentException; {:try_start_parse .. :try_end_parse} :catch_invalid

    if-eqz v0, :missing
    invoke-static {p0}, Ljadx/tests/integration/trycatch/TestProtectedConditionTerminal;->after(I)V
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v1
    return v1

    :missing
    :try_start_missing
    invoke-static {}, Ljadx/tests/integration/trycatch/TestProtectedConditionTerminal;->missing()V
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
    :try_end_missing
    .catch Ljava/lang/IllegalArgumentException; {:try_start_missing .. :try_end_missing} :catch_invalid

    :catch_invalid
    move-exception v0
    invoke-static {}, Ljadx/tests/integration/trycatch/TestProtectedConditionTerminal;->recover()I
    move-result v1
    return v1
.end method

.method public static check()V
    .registers 3

    const/4 v0, 0x0
    invoke-static {v0}, Ltrycatch/TestProtectedConditionTerminal;->run(I)I
    move-result v1
    const/16 v2, 0x2a
    if-ne v1, v2, :fail

    const/4 v0, 0x1
    invoke-static {v0}, Ltrycatch/TestProtectedConditionTerminal;->run(I)I
    move-result v1
    const/16 v2, 0x63
    if-ne v1, v2, :fail

    const/4 v0, 0x2
    invoke-static {v0}, Ltrycatch/TestProtectedConditionTerminal;->run(I)I
    move-result v1
    if-ne v1, v2, :fail

    const/4 v0, 0x3
    :try_start_after
    invoke-static {v0}, Ltrycatch/TestProtectedConditionTerminal;->run(I)I
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
