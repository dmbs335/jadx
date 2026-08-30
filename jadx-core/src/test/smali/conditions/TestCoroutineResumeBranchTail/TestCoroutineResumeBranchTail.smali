.class public final Lconditions/TestCoroutineResumeBranchTail;
.super Ljava/lang/Object;

.method public static final collect(Ljava/util/Iterator;Ljava/util/Collection;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10

    instance-of v0, p2, Lconditions/TestCoroutineResumeBranchTail$collect$1;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;
    iget v2, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;
    invoke-direct {v0, p2}, Lconditions/TestCoroutineResumeBranchTail$collect$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p2, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-eq v2, v3, :resume_map
    const/4 v4, 0x2
    if-eq v2, v4, :resume_emit

    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :resume_map
    iget-object p0, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->L$0:Ljava/lang/Object;
    check-cast p0, Ljava/util/Iterator;
    iget-object p1, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->L$1:Ljava/lang/Object;
    check-cast p1, Ljava/util/Collection;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p2
    goto :projection

    :resume_emit
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :done

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :loop
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v4
    if-eqz v4, :emit
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v4
    iput-object p0, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->L$1:Ljava/lang/Object;
    iput v3, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->label:I
    invoke-static {v4, v0}, Lconditions/TestCoroutineResumeBranchTail;->map(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v1, :projection
    goto :suspended

    :projection
    check-cast v4, Ljava/lang/String;
    if-eqz v4, :merge
    invoke-interface {p1, v4}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z

    :merge
    const/4 v5, 0x0
    goto :loop

    :emit
    const/4 v4, 0x2
    iput v4, v0, Lconditions/TestCoroutineResumeBranchTail$collect$1;->label:I
    invoke-static {p1, v0}, Lconditions/TestCoroutineResumeBranchTail;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v1, :done

    :suspended
    return-object v1

    :done
    sget-object v4, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v4
.end method

.method private static map(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method

.method private static emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method
