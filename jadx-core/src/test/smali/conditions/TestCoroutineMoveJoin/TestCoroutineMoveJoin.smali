.class public final Lconditions/TestCoroutineMoveJoin;
.super Ljava/lang/Object;

.method public static final selectLoop(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 7

    instance-of v0, p0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;
    if-eqz v0, :new_state

    move-object v0, p0
    check-cast v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;
    iget v2, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state

    sub-int/2addr v2, v3
    iput v2, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;
    invoke-direct {v0, p0}, Lconditions/TestCoroutineMoveJoin$selectLoop$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p0, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->label:I
    const/4 v3, 0x1

    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v5, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/lang/String;
    invoke-static {p0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, v5
    goto :completion

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string v0, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const-string v4, "seed"

    :loop
    iput-object v4, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Lconditions/TestCoroutineMoveJoin$selectLoop$1;->label:I
    invoke-static {v4, v0}, Lconditions/TestCoroutineMoveJoin;->select(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p0
    if-ne p0, v1, :completion
    return-object v1

    :completion
    move-object v4, p0
    invoke-static {v4}, Lconditions/TestCoroutineMoveJoin;->consume(Ljava/lang/Object;)V
    goto :loop
.end method

.method private static consume(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static select(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method
