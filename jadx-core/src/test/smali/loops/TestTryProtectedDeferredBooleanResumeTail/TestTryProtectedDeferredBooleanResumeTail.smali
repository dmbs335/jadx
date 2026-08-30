.class public Lloops/TestTryProtectedDeferredBooleanResumeTail;
.super Ljava/lang/Object;

.method public static final clear(Ljava/util/concurrent/ConcurrentHashMap;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 17

    move-object/from16 v0, p3
    instance-of v1, v0, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;
    if-eqz v1, :new_continuation
    move-object v1, v0
    check-cast v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;
    iget v2, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_continuation
    sub-int/2addr v2, v3
    iput v2, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;
    invoke-direct {v1, v0}, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object v0, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    const/4 v4, 0x0
    const/4 v5, 0x1
    if-eqz v3, :initial
    if-ne v3, v5, :bad_state

    iget-wide v6, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->J$1:J
    iget-boolean v3, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->Z$0:Z
    iget v8, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->I$0:I
    iget-wide v9, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->J$0:J
    iget-object v11, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$6:Ljava/lang/Object;
    check-cast v11, Ljava/util/concurrent/ConcurrentHashMap;
    iget-object v11, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$5:Ljava/lang/Object;
    check-cast v11, Lkotlinx/coroutines/CompletableDeferred;
    iget-object v11, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$4:Ljava/lang/Object;
    iget-object v12, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$3:Ljava/lang/Object;
    check-cast v12, Ljava/util/Map$Entry;
    iget-object v12, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$2:Ljava/lang/Object;
    check-cast v12, Ljava/util/Iterator;
    iget-object v13, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$1:Ljava/lang/Object;
    check-cast v13, Ljava/util/Map;
    iget-object v14, v1, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$0:Ljava/lang/Object;
    check-cast v14, Ljava/util/concurrent/ConcurrentHashMap;

    :resume_try_start
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :resume_try_end
    .catchall {:resume_try_start .. :resume_try_end} :resume_catch
    goto/16 :after_await

    :resume_catch
    move-exception v0
    goto/16 :result_failure

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :initial
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-interface/range {p0 .. p0}, Ljava/util/Map;->entrySet()Ljava/util/Set;
    move-result-object v0
    invoke-interface {v0}, Ljava/util/Set;->iterator()Ljava/util/Iterator;
    move-result-object v0
    move-object/from16 v3, p0
    move-wide/from16 v6, p1
    move-object v12, v0
    move-object v8, v1
    move v9, v4
    move-object v1, v3

    :loop_header
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :done
    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/Map$Entry;
    invoke-interface {v0}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;
    move-result-object v11
    invoke-interface {v0}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;
    move-result-object v10
    check-cast v10, Lkotlinx/coroutines/CompletableDeferred;

    :source_try_start
    sget-object v13, Lkotlin/Result;->Companion:Lkotlin/Result$Companion;
    invoke-interface {v10}, Lkotlinx/coroutines/Job;->isCancelled()Z
    move-result v13
    invoke-interface {v10}, Lkotlinx/coroutines/Job;->isCompleted()Z
    move-result v14
    if-eqz v14, :not_expired

    iput-object v1, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$0:Ljava/lang/Object;
    invoke-static {v3}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v14
    iput-object v14, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$1:Ljava/lang/Object;
    iput-object v12, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$2:Ljava/lang/Object;
    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    iput-object v0, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$3:Ljava/lang/Object;
    iput-object v11, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$4:Ljava/lang/Object;
    invoke-static {v10}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    iput-object v0, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$5:Ljava/lang/Object;
    invoke-static {v1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    iput-object v0, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->L$6:Ljava/lang/Object;
    iput-wide v6, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->J$0:J
    iput v9, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->I$0:I
    iput v4, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->I$1:I
    iput v4, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->I$2:I
    iput-boolean v13, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->Z$0:Z
    iput-wide v6, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->J$1:J
    iput v5, v8, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    invoke-interface {v10, v8}, Lkotlinx/coroutines/Deferred;->await(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    :source_try_end
    .catchall {:source_try_start .. :source_try_end} :source_catch
    if-ne v0, v2, :direct_completion
    return-object v2

    :direct_completion
    move v10, v13
    move-object v13, v3
    move v3, v10
    move-object v14, v1
    move-object v1, v8
    move v8, v9
    move-wide v9, v6

    :after_await
    :decision_try_start
    check-cast v0, Lloops/DeferredData;
    invoke-virtual {v0}, Lloops/DeferredData;->getExpires()J
    move-result-wide v15
    cmp-long v0, v6, v15
    if-ltz v0, :not_yet_expired
    move v0, v5
    goto :boolean_or

    :not_yet_expired
    move-object v6, v13
    move v13, v3
    move-object v3, v6
    move-wide v6, v9
    move v9, v8
    move-object v8, v1
    move-object v1, v14
    goto :not_expired

    :source_catch
    move-exception v0
    move-object v14, v1
    move-object v13, v3
    move-object v1, v8
    move v8, v9
    move-wide v9, v6
    goto :result_failure

    :not_expired
    move v0, v13
    move-object v13, v3
    move v3, v0
    move-object v14, v1
    move v0, v4
    move-object v1, v8
    move v8, v9
    move-wide v9, v6

    :boolean_or
    if-nez v3, :boolean_true
    if-eqz v0, :boolean_false

    :boolean_true
    move v0, v5
    goto :box_result

    :boolean_false
    move v0, v4

    :box_result
    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
    move-result-object v0
    invoke-static {v0}, Lkotlin/Result;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    :decision_try_end
    .catchall {:decision_try_start .. :decision_try_end} :resume_catch
    goto :restore_loop_state

    :result_failure
    sget-object v3, Lkotlin/Result;->Companion:Lkotlin/Result$Companion;
    invoke-static {v0}, Lkotlin/ResultKt;->createFailure(Ljava/lang/Throwable;)Ljava/lang/Object;
    move-result-object v0
    invoke-static {v0}, Lkotlin/Result;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0

    :restore_loop_state
    move-wide v6, v9
    move-object v3, v13
    move v9, v8
    move-object v8, v1
    move-object v1, v14
    invoke-static {v5}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
    move-result-object v10
    invoke-static {v0}, Lkotlin/Result;->isFailure-impl(Ljava/lang/Object;)Z
    move-result v13
    if-eqz v13, :unbox_result
    move-object v0, v10

    :unbox_result
    check-cast v0, Ljava/lang/Boolean;
    invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v0
    if-eqz v0, :loop_header
    invoke-virtual {v1, v11}, Ljava/util/concurrent/ConcurrentHashMap;->remove(Ljava/lang/Object;)Ljava/lang/Object;
    goto :loop_header

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
