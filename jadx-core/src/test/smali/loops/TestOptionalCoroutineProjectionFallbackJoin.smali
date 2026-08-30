.class public Lloops/TestOptionalCoroutineProjectionFallbackJoin;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private L$0:Ljava/lang/Object;
.field private final projection:Lkotlin/jvm/functions/Function2;
.field private final source:Ljava/lang/Object;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v4
    iget v5, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->label:I
    packed-switch v5, :state_switch
    goto :bad_state

    :resume
    iget-object v1, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v0, p1
    goto :nullable_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v2, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->source:Ljava/lang/Object;
    iget-object v3, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->projection:Lkotlin/jvm/functions/Function2;
    if-eqz v3, :fallback

    move-object v1, v2
    iput-object v1, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->L$0:Ljava/lang/Object;
    const/4 v5, 0x1
    iput v5, p0, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->label:I
    invoke-interface {v3, v1, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended

    :nullable_result
    if-nez v0, :result
    move-object v2, v1

    :fallback
    invoke-static {v2}, Lloops/TestOptionalCoroutineProjectionFallbackJoin;->fallback(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0

    :result
    return-object v0

    :suspended
    return-object v4

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_switch
    .packed-switch 0x0
        :initial
        :resume
    .end packed-switch
.end method

.method private static fallback(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 1

    return-object p0
.end method
