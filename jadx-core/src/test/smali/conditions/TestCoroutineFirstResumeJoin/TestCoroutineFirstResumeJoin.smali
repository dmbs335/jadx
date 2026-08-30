.class public final Lconditions/TestCoroutineFirstResumeJoin;
.super Ljava/lang/Object;

.method public static final process(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    instance-of v0, p1, Lconditions/TestCoroutineFirstResumeJoin$process$1;
    if-eqz v0, :new_state
    move-object v0, p1
    check-cast v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;
    iget v2, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;
    invoke-direct {v0, p1}, Lconditions/TestCoroutineFirstResumeJoin$process$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p1, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-eq v2, v3, :resume_load
    const/4 v4, 0x2
    if-eq v2, v4, :resume_delete
    const/4 v4, 0x3
    if-eq v2, v4, :resume_save

    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :resume_save
    iget-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :done

    :resume_delete
    iget-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :save

    :resume_load
    iget-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loaded

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iput-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    invoke-static {p0, v0}, Lconditions/TestCoroutineFirstResumeJoin;->load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :loaded
    return-object v1

    :loaded
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result p1
    and-int/lit8 p1, p1, 0x1
    if-eqz p1, :save
    iput-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    const/4 p1, 0x2
    iput p1, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    invoke-static {v0}, Lconditions/TestCoroutineFirstResumeJoin;->delete(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :save
    return-object v1

    :save
    iput-object p0, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->L$0:Ljava/lang/Object;
    const/4 p1, 0x3
    iput p1, v0, Lconditions/TestCoroutineFirstResumeJoin$process$1;->label:I
    invoke-static {p0, v0}, Lconditions/TestCoroutineFirstResumeJoin;->save(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :done
    return-object v1

    :done
    return-object p0
.end method

.method private static load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method

.method private static delete(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static save(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method
