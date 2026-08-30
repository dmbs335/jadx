.class public Lloops/TestCoroutineConditionalNoEffectResumeTail;
.super Ljava/lang/Object;

.field public label:I
.field public result:Ljava/lang/Object;
.field public iterator:Ljava/util/Iterator;
.field public key:Ljava/lang/Object;
.field public saved:Ljava/lang/String;

.method public static run(Ljava/util/Iterator;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 8

    check-cast p2, Lloops/TestCoroutineConditionalNoEffectResumeTail;
    move-object v0, p2
    iget v1, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->label:I
    iget-object v2, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3
    if-eqz v1, :initial

    const/4 v6, 0x1
    if-ne v1, v6, :bad_state
    iget-object v4, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->iterator:Ljava/util/Iterator;
    iget-object p1, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->key:Ljava/lang/Object;
    iget-object v5, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->saved:Ljava/lang/String;
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_join

    :initial
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p0
    const-string v5, "keep"

    :loop_header
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    if-eqz p1, :resume_join

    move-object v6, v5
    iput-object v4, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->iterator:Ljava/util/Iterator;
    iput-object p1, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->key:Ljava/lang/Object;
    iput-object v5, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->saved:Ljava/lang/String;
    const/4 v7, 0x1
    iput v7, v0, Lloops/TestCoroutineConditionalNoEffectResumeTail;->label:I
    invoke-static {p1, v0}, Lloops/TestCoroutineConditionalNoEffectResumeTail;->suspendSet(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v3, :suspended
    goto :result_tail

    :resume_join
    move-object v6, v5

    :result_tail
    move-object v5, v6
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
.end method

.method public static suspendSet(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
