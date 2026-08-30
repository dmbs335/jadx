.class public final Lloops/TestCoroutineShortCircuitProjectionJoin;
.super Ljava/lang/Object;

.field private element:Ljava/lang/Object;
.field private label:I

.method private static action()V
    .registers 0
    return-void
.end method

.method private static first()Z
    .registers 1
    const/4 v0, 0x0
    return v0
.end method

.method private static second()Z
    .registers 1
    const/4 v0, 0x0
    return v0
.end method

.method private static suspendAction(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static third()Z
    .registers 1
    const/4 v0, 0x0
    return v0
.end method

.method public final loop(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    iget v0, p0, Lloops/TestCoroutineShortCircuitProjectionJoin;->label:I
    const/4 v1, 0x0
    const/4 v2, 0x3
    if-eqz v0, :initial
    const/4 v3, 0x1
    if-eq v0, v3, :resume_header

    :resume_action
    goto :action_tail

    :resume_header
    goto :loop

    :initial
    iput v1, p0, Lloops/TestCoroutineShortCircuitProjectionJoin;->label:I

    :loop
    invoke-static {}, Lloops/TestCoroutineShortCircuitProjectionJoin;->first()Z
    move-result v0
    if-nez v0, :projection

    invoke-static {}, Lloops/TestCoroutineShortCircuitProjectionJoin;->second()Z
    move-result v0
    if-nez v0, :projection

    invoke-static {}, Lloops/TestCoroutineShortCircuitProjectionJoin;->third()Z
    move-result v0
    if-nez v0, :projection

    invoke-static {p1}, Lloops/TestCoroutineShortCircuitProjectionJoin;->suspendAction(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    if-eqz v0, :suspended

    :action_tail
    invoke-static {}, Lloops/TestCoroutineShortCircuitProjectionJoin;->action()V

    :projection
    move-object v3, p0
    move v4, v1
    iget-object v5, v3, Lloops/TestCoroutineShortCircuitProjectionJoin;->element:Ljava/lang/Object;
    if-eqz v5, :done
    add-int/lit8 v1, v4, 0x1
    if-lt v1, v2, :loop

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :suspended
    return-object p1
.end method
