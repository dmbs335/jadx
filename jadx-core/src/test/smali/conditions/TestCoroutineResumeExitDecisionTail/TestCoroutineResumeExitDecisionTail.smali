.class public final Lconditions/TestCoroutineResumeExitDecisionTail;
.super Ljava/lang/Object;

.method public static final lastSuccess([Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p1, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;
    if-eqz v0, :new_state
    move-object v0, p1
    check-cast v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;
    iget v2, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;
    invoke-direct {v0, p1}, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p1, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-eq v2, v3, :resume

    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :resume
    iget-object p0, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->L$0:Ljava/lang/Object;
    check-cast p0, [Lkotlin/jvm/functions/Function1;
    iget v3, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v6, p1
    move-object v9, p0
    goto :projection

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v3, 0x0

    :loop
    array-length v4, p0
    if-ge v3, v4, :none
    aget-object v5, p0, v3
    iput-object p0, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->I$0:I
    const/4 v2, 0x1
    iput v2, v0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    invoke-interface {v5, v0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    if-eq v6, v1, :suspended

    :direct_bridge
    move-object v9, p0

    :projection
    check-cast v6, Lconditions/TestCoroutineResumeExitDecisionTail$Result;
    invoke-virtual {v6}, Lconditions/TestCoroutineResumeExitDecisionTail$Result;->isSuccess()Z
    move-result v7
    if-eqz v7, :return_result
    invoke-static {v9}, Lconditions/TestCoroutineResumeExitDecisionTail;->getLastIndex([Ljava/lang/Object;)I
    move-result v8
    if-eq v3, v8, :return_result
    add-int/lit8 v3, v3, 0x1
    move-object p0, v9
    goto :loop

    :return_result
    return-object v6

    :suspended
    return-object v1

    :none
    const/4 v6, 0x0
    return-object v6
.end method

.method private static getLastIndex([Ljava/lang/Object;)I
    .registers 2
    array-length v0, p0
    add-int/lit8 v0, v0, -0x1
    return v0
.end method
