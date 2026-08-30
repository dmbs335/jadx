.class public final Lloops/TestCoroutineSequenceYieldIteratorCompletion;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field private I$0:I
.field private I$1:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 1
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 9

    iget-object v0, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$0:Ljava/lang/Object;
    check-cast v0, Lkotlin/sequences/SequenceScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->label:I
    const/4 v3, 0x0
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-ne v2, v4, :bad_state

    iget v2, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->I$1:I
    iget v5, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$2:Ljava/lang/Object;
    check-cast v6, Ljava/util/Iterator;
    iget-object v7, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$1:Ljava/lang/Object;
    check-cast v7, Ljava/util/ArrayList;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :completion_join

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "bad coroutine state"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v7, Ljava/util/ArrayList;
    invoke-direct {v7}, Ljava/util/ArrayList;-><init>()V
    const/4 v6, 0x0
    check-cast v6, Ljava/util/Iterator;
    move v2, v3
    move v5, v3
    goto :loop_header

    :completion_join
    move v5, v2
    goto :post_completion

    :post_completion
    invoke-virtual {v7}, Ljava/util/ArrayList;->clear()V

    :loop_header
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z
    move-result v8
    if-eqz v8, :done
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v8
    invoke-virtual {v7, v8}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    invoke-virtual {v7}, Ljava/util/ArrayList;->size()I
    move-result v8
    const/4 p1, 0x2
    if-lt v8, p1, :loop_header

    iput-object v0, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$0:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$1:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->L$2:Ljava/lang/Object;
    iput v5, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->I$0:I
    iput v2, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->I$1:I
    iput v4, p0, Lloops/TestCoroutineSequenceYieldIteratorCompletion;->label:I
    invoke-virtual {v0, v7, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v8
    if-ne v8, v1, :completion_join
    return-object v1

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
