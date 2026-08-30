.class public Lloops/TestCompleteSmallIfChainCoroutineCompletions;
.super Ljava/lang/Object;

.field private label:I

.method private static awaitContent()Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static shouldWrite()Z
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method private static writeByte()Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_await
    const/4 v1, 0x2
    if-eq v0, v1, :resume_write
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_write
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->throwOnFailure(Ljava/lang/Object;)V
    goto :write_done

    :resume_await
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->throwOnFailure(Ljava/lang/Object;)V
    goto :await_result

    :initial
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->throwOnFailure(Ljava/lang/Object;)V

    :loop_header
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->label:I
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->awaitContent()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, p2, :suspended

    :await_result
    move-object v2, p1
    check-cast v2, Ljava/lang/Boolean;
    invoke-virtual {v2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v3
    if-eqz v3, :no_match
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->shouldWrite()Z
    move-result v3
    if-eqz v3, :loop_header

    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->label:I
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineCompletions;->writeByte()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, p2, :suspended

    :write_done
    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
    return-object v0

    :no_match
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0

    :suspended
    return-object p2
.end method
