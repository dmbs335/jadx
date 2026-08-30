.class public final Lconditions/TestCoroutineResumeMoveCastNullableAddTail;
.super Ljava/lang/Object;

.method public static final map(Ljava/util/List;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 13

    instance-of v0, p2, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;
    iget v3, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->label:I
    const/high16 v4, -0x80000000
    and-int v5, v3, v4
    if-eqz v5, :new_state
    sub-int/2addr v3, v4
    iput v3, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;
    invoke-direct {v0, p2}, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v1, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->label:I
    if-eqz v3, :initial
    const/4 v4, 0x1
    if-ne v3, v4, :bad_state
    iget-object p0, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$0:Ljava/lang/Object;
    check-cast p0, Ljava/util/List;
    iget-object v5, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$1:Ljava/lang/Object;
    check-cast v5, Ljava/util/Collection;
    iget-object v6, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$2:Ljava/lang/Object;
    check-cast v6, Ljava/util/Iterator;
    iget-object p1, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$3:Ljava/lang/Object;
    check-cast p1, Lkotlin/jvm/functions/Function2;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v8, v1
    goto :projection

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v5, Ljava/util/ArrayList;
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v6

    :loop
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z
    move-result v7
    if-eqz v7, :done
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    iput-object p0, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$0:Ljava/lang/Object;
    iput-object v5, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$1:Ljava/lang/Object;
    iput-object v6, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$2:Ljava/lang/Object;
    iput-object p1, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->L$3:Ljava/lang/Object;
    const/4 v9, 0x1
    iput v9, v0, Lconditions/TestCoroutineResumeMoveCastNullableAddTail$map$1;->label:I
    invoke-interface {p1, v7, v0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v8
    if-eq v8, v2, :suspended

    :direct_bridge
    move-object v9, p0

    :projection
    move-object v9, v8
    check-cast v9, Ljava/lang/String;
    if-eqz v9, :merge
    invoke-interface {v5, v9}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z

    :merge
    move-object v9, p0
    goto :loop

    :suspended
    return-object v2

    :done
    return-object v5
.end method
