.class public final Lloops/TestCoroutineNullableSerializeResultLoopTail;
.super Ljava/lang/Object;

.method public static final convert(Ljava/util/Iterator;Ltest/ContentConverter;Lorg/slf4j/Logger;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 13

    instance-of v0, p3, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;
    if-eqz v0, :new_continuation
    move-object v0, p3
    check-cast v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;
    iget v2, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    const/high16 v3, -0x80000000
    and-int v8, v2, v3
    if-eqz v8, :new_continuation
    sub-int/2addr v2, v3
    iput v2, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;
    invoke-direct {v0, p3}, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v4, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    if-eqz v2, :initial
    const/4 v3, 0x1
    if-ne v2, v3, :bad_state

    iget-object p2, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$2:Ljava/lang/Object;
    check-cast p2, Lorg/slf4j/Logger;
    iget-object p1, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$1:Ljava/lang/Object;
    check-cast p1, Ltest/ContentConverter;
    iget-object p0, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$0:Ljava/lang/Object;
    check-cast p0, Ljava/util/Iterator;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :result_join

    :initial
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :loop
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v8
    if-eqz v8, :no_result
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v5

    iput-object p0, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$1:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->L$2:Ljava/lang/Object;
    const/4 v3, 0x1
    iput v3, v0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    invoke-interface {p1, v5, v0}, Ltest/ContentConverter;->serialize(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v6
    if-ne v6, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object v4, v6

    :result_join
    check-cast v4, Ltest/OutgoingContent;
    if-eqz v4, :result_decision
    const-string v6, "serialized"
    invoke-interface {p2, v6}, Lorg/slf4j/Logger;->trace(Ljava/lang/String;)V

    :result_decision
    if-nez v4, :success
    const/4 v8, 0x1
    goto :loop

    :success
    move-object v7, v4
    goto :exit_join

    :no_result
    const/4 v7, 0x0

    :exit_join
    if-eqz v7, :failed
    return-object v7

    :failed
    new-instance v8, Ljava/lang/IllegalStateException;
    invoke-direct {v8}, Ljava/lang/IllegalStateException;-><init>()V
    throw v8

    :bad_state
    new-instance v8, Ljava/lang/IllegalStateException;
    invoke-direct {v8}, Ljava/lang/IllegalStateException;-><init>()V
    throw v8
.end method
