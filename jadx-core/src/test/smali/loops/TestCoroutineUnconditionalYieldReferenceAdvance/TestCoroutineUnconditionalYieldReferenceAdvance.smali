.class public final Lloops/TestCoroutineUnconditionalYieldReferenceAdvance;
.super Ljava/lang/Object;

.method public static run(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 11

    instance-of v0, p2, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;
    if-eqz v0, :new_state
    move-object v7, p2
    check-cast v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;
    iget v0, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->label:I
    const/high16 v1, -0x80000000
    and-int v2, v0, v1
    if-eqz v2, :new_state
    sub-int/2addr v0, v1
    iput v0, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->label:I
    goto :dispatch

    :new_state
    new-instance v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;
    invoke-direct {v7, p2}, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v0, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :invalid

    iget-object v4, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->L$1:Ljava/lang/Object;
    check-cast v4, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    iget-object v5, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlin/sequences/SequenceScope;
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :advance

    :invalid
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p1
    move-object v5, p0

    :loop_header
    if-eqz v4, :done
    invoke-interface {v4}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getStackTraceElement()Ljava/lang/StackTraceElement;
    move-result-object v6
    if-eqz v6, :advance

    iput-object v5, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->L$0:Ljava/lang/Object;
    iput-object v4, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->L$1:Ljava/lang/Object;
    iput v3, v7, Lloops/TestCoroutineUnconditionalYieldReferenceAdvance$State;->label:I
    invoke-virtual {v5, v6, v7}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    goto :suspended

    :advance
    invoke-interface {v4}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getCallerFrame()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    move-result-object v4
    if-eqz v4, :done
    goto :loop_header

    :suspended
    return-object v1

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
