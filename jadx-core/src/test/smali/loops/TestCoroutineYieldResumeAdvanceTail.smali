.class public final Lloops/TestCoroutineYieldResumeAdvanceTail;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;
.source "ScatterMap.kt"

.field I$0:I
.field I$1:I
.field I$2:I
.field private synthetic L$0:Ljava/lang/Object;
.field label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0

    iget v1, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->label:I
    if-eqz v1, :initial

    const/4 v2, 0x1
    if-ne v1, v2, :invalid_state

    iget v1, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$2:I
    iget v2, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$1:I
    iget v3, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$0:I
    iget-object v4, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlin/sequences/SequenceScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :bridge

    :invalid_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to resume before invoke"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v4, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlin/sequences/SequenceScope;
    const/4 v3, 0x0

    :outer_header
    const/4 v5, 0x2
    if-ge v3, v5, :done
    const/4 v2, 0x2
    const/4 v1, 0x0

    :inner_header
    if-ge v1, v2, :outer_advance
    and-int/lit8 v5, v1, 0x1
    if-nez v5, :bridge

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v5
    iput-object v4, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$0:I
    iput v2, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$1:I
    iput v1, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->I$2:I
    const/4 v6, 0x1
    iput v6, p0, Lloops/TestCoroutineYieldResumeAdvanceTail;->label:I
    invoke-virtual {v4, v5, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-eq v5, v0, :suspended
    goto :advance

    :bridge
    move v7, v1

    :advance
    add-int/lit8 v7, v7, 0x1
    move v1, v7
    goto :inner_header

    :outer_advance
    add-int/lit8 v3, v3, 0x1
    goto :outer_header

    :suspended
    return-object v0

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
