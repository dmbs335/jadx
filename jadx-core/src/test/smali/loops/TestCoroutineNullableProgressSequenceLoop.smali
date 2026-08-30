.class public Lloops/TestCoroutineNullableProgressSequenceLoop;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private label:I
.field private result:Ljava/lang/Object;

.method public run(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 12

    iget-object v0, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->label:I
    const/4 v9, 0x1
    if-eqz v2, :initial
    if-ne v2, v9, :bad_state

    iget-object v3, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$2:Ljava/lang/Object;
    check-cast v3, Lloops/TestCoroutineNullableProgressSequenceLoop;
    iget-object v4, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$1:Ljava/lang/Object;
    check-cast v4, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    iget-object v5, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlin/sequences/SequenceScope;
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :move_join

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    const-string v3, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {v2, v3}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v2

    :initial
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v3, p0
    move-object v4, p2
    move-object v5, p1
    move-object v6, v4
    move-object v7, v3
    move-object v8, v5

    :loop_header
    if-eqz v6, :done
    invoke-interface {v6}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getStackTraceElement()Ljava/lang/StackTraceElement;
    move-result-object v10
    if-eqz v10, :progress

    iput-object v8, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->L$2:Ljava/lang/Object;
    iput v9, p0, Lloops/TestCoroutineNullableProgressSequenceLoop;->label:I
    move-object v3, v7
    move-object v4, v6
    move-object v5, v8
    invoke-virtual {v8, v10, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v11
    if-ne v11, v1, :direct_bridge
    return-object v1

    :direct_bridge
    move-object v0, v11

    :move_join
    move-object v6, v4
    move-object v7, v3
    move-object v8, v5

    :progress
    invoke-interface {v6}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getCallerFrame()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    move-result-object v6
    if-eqz v6, :done
    goto :loop_header

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
