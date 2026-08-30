.class public final Lconditions/TestCoroutineResumeBooleanProjectionCleanup;
.super Ljava/lang/Object;

.method public static final loop(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    instance-of v0, p0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;
    if-eqz v0, :new_state

    move-object v0, p0
    check-cast v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;
    iget v3, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->label:I
    const/high16 v4, -0x80000000
    and-int v5, v3, v4
    if-eqz v5, :new_state

    sub-int/2addr v3, v4
    iput v3, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;
    invoke-direct {v0, p0}, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v1, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->label:I
    const/4 v4, 0x1

    if-eqz v3, :initial
    if-ne v3, v4, :bad_state

    iget-object v5, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->L$1:Ljava/lang/Object;
    iget-object p0, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->L$0:Ljava/lang/Object;
    goto :resume

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    :try_start
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    move-object v5, p0
    goto :loop

    :resume
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :projection

    :loop
    iput-object p0, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->L$0:Ljava/lang/Object;
    iput-object v5, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->L$1:Ljava/lang/Object;
    iput v4, v0, Lconditions/TestCoroutineResumeBooleanProjectionCleanup$loop$1;->label:I
    invoke-static {v5, v0}, Lconditions/TestCoroutineResumeBooleanProjectionCleanup;->hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, v2, :projection
    return-object v2

    :projection
    check-cast v1, Ljava/lang/Boolean;
    invoke-virtual {v1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v3
    if-eqz v3, :done
    invoke-static {v5}, Lconditions/TestCoroutineResumeBooleanProjectionCleanup;->next(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    goto :loop
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

    :done
    const/4 v6, 0x0
    invoke-static {p0, v6}, Lconditions/TestCoroutineResumeBooleanProjectionCleanup;->cancelConsumed(Ljava/lang/Object;Ljava/lang/Throwable;)V
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1

    :catch_all
    move-exception v1
    invoke-static {p0, v1}, Lconditions/TestCoroutineResumeBooleanProjectionCleanup;->cancelConsumed(Ljava/lang/Object;Ljava/lang/Throwable;)V
    throw v1
.end method

.method private static hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3

    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static cancelConsumed(Ljava/lang/Object;Ljava/lang/Throwable;)V
    .registers 2
    return-void
.end method
