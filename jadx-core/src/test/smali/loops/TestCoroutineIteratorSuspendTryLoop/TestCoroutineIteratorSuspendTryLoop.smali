.class public Lloops/TestCoroutineIteratorSuspendTryLoop;
.super Ljava/lang/Object;

.method public static clear(Ljava/util/Map;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 12

    move-object/from16 v0, p3
    instance-of v1, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;
    if-eqz v1, :new_continuation
    move-object v1, v0
    check-cast v1, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;
    iget v2, v1, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_continuation
    sub-int/2addr v2, v3
    iput v2, v1, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v1, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;
    invoke-direct {v1, v0}, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    move-object v0, v1
    iget v1, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    iget-object v2, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3

    if-eqz v1, :initial
    const/4 v4, 0x1
    if-ne v1, v4, :bad_state

    iget-object p0, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->map:Ljava/util/Map;
    iget-object v4, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->iterator:Ljava/util/Iterator;
    iget-object v5, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->key:Ljava/lang/Object;
    iget-wide p1, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->targetTime:J
    iget-boolean v6, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->isCancelled:Z
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_await

    :initial
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-interface {p0}, Ljava/util/Map;->entrySet()Ljava/util/Set;
    move-result-object v4
    invoke-interface {v4}, Ljava/util/Set;->iterator()Ljava/util/Iterator;
    move-result-object v4

    :loop_header
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v7
    if-eqz v7, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Ljava/util/Map$Entry;
    invoke-interface {v7}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;
    move-result-object v5
    invoke-interface {v7}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Lkotlinx/coroutines/Deferred;

    :try_start
    invoke-interface {v7}, Lkotlinx/coroutines/Job;->isCancelled()Z
    move-result v6
    if-nez v6, :expired
    invoke-interface {v7}, Lkotlinx/coroutines/Job;->isCompleted()Z
    move-result v8
    if-eqz v8, :not_expired

    iput-object p0, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->map:Ljava/util/Map;
    iput-object v4, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->iterator:Ljava/util/Iterator;
    iput-object v5, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->key:Ljava/lang/Object;
    iput-wide p1, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->targetTime:J
    iput-boolean v6, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->isCancelled:Z
    const/4 v8, 0x1
    iput v8, v0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    invoke-interface {v7, v0}, Lkotlinx/coroutines/Deferred;->await(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v3, :suspended

    :after_await
    invoke-virtual {v2}, Ljava/lang/Object;->hashCode()I
    move-result v8
    int-to-long v8, v8
    cmp-long v10, p1, v8
    if-gez v10, :expired

    :not_expired
    const/4 v8, 0x0
    goto :try_end

    :expired
    const/4 v8, 0x1

    :try_end
    goto :remove_decision

    :catch
    move-exception v7
    const/4 v8, 0x1

    :remove_decision
    if-eqz v8, :loop_header
    invoke-interface {p0, v5}, Ljava/util/Map;->remove(Ljava/lang/Object;)Ljava/lang/Object;
    goto :loop_header

    :suspended
    return-object v3

    :done
    sget-object v2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v2

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    .catch Ljava/lang/Throwable; {:try_start .. :try_end} :catch
.end method
