.class public Lloops/TestCoroutineNullableFactoryRetryLoop;
.super Ljava/lang/Object;

.method public final retry(Ltest/Factory;Ltest/Event;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p3, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;
    if-eqz v0, :new_continuation

    move-object v0, p3
    check-cast v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;
    iget v2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_continuation

    sub-int/2addr v2, v3
    iput v2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;
    invoke-direct {v0, p0, p3}, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;-><init>(Lloops/TestCoroutineNullableFactoryRetryLoop;Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object p3, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->label:I
    if-eqz v2, :initial

    const/4 v3, 0x1
    if-ne v2, v3, :bad_state

    iget v2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->I$0:I
    iget-object p1, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    check-cast p1, Ltest/Factory;
    iget-object p2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    check-cast p2, Ltest/Event;
    iget-object v4, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$2:Ljava/lang/Object;
    check-cast v4, Ltest/Candidate;
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :projection

    :initial
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x0

    :factory
    invoke-interface {p1, v2}, Ltest/Factory;->next(I)Ltest/Candidate;
    move-result-object v4
    if-eqz v4, :no_candidate

    add-int/lit8 v5, v2, 0x1
    invoke-interface {p2, v4}, Ltest/Event;->start(Ltest/Candidate;)V
    iput-object p1, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->L$2:Ljava/lang/Object;
    iput v5, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->I$0:I
    const/4 v3, 0x1
    iput v3, v0, Lloops/TestCoroutineNullableFactoryRetryLoop$retry$1;->label:I
    invoke-interface {v4, v0}, Ltest/Candidate;->run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v6
    if-ne v6, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object p3, v6
    move v2, v5

    :projection
    check-cast p3, Ltest/Result;
    invoke-interface {p2, v4, p3}, Ltest/Event;->end(Ltest/Candidate;Ltest/Result;)V
    if-nez p3, :done
    goto :factory

    :done
    return-object p3

    :no_candidate
    new-instance v7, Ljava/lang/IllegalStateException;
    invoke-direct {v7}, Ljava/lang/IllegalStateException;-><init>()V
    throw v7

    :bad_state
    new-instance v7, Ljava/lang/IllegalStateException;
    invoke-direct {v7}, Ljava/lang/IllegalStateException;-><init>()V
    throw v7
.end method
