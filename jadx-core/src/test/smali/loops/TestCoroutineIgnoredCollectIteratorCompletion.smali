.class public final Lloops/TestCoroutineIgnoredCollectIteratorCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 12

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->label:I
    const/4 v2, 0x1
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    iget v9, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->I$0:I
    iget-object v4, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/List;
    iget-object v5, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/util/Iterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v11, 0x1
    move-object v6, v4
    move-object v7, v5
    goto :result_join

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v4, Ljava/util/ArrayList;
    invoke-direct {v4}, Ljava/util/ArrayList;-><init>()V
    invoke-interface {v4}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v5
    const/4 v3, 0x0
    const/4 v11, 0x1

    :loop
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
    move-result v8
    if-eqz v8, :done
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v8
    add-int/lit8 v9, v3, 0x1
    invoke-static {v8, v11}, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->shouldCollect(Ljava/lang/Object;Z)Z
    move-result v10
    if-eqz v10, :ordinary_latch

    iput-object v5, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->L$1:Ljava/lang/Object;
    iput v9, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->I$0:I
    iput v2, p0, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->label:I
    invoke-static {v8}, Lloops/TestCoroutineIgnoredCollectIteratorCompletion;->flowFor(Ljava/lang/Object;)Lkotlinx/coroutines/flow/Flow;
    move-result-object v10
    const/4 v1, 0x0
    invoke-interface {v10, v1, p0}, Lkotlinx/coroutines/flow/Flow;->collect(Lkotlinx/coroutines/flow/FlowCollector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v8
    if-ne v8, v0, :direct_complete
    return-object v0

    :direct_complete
    move-object v6, v4
    move-object v7, v5
    move v9, v9

    :result_join
    move-object v4, v6
    move-object v5, v7
    move v3, v9
    goto :rotation_tail

    :ordinary_latch
    move-object v6, v4
    move-object v7, v5
    goto :rotation_tail

    :rotation_tail
    move v3, v9
    const/4 v11, 0x1
    goto :loop

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static shouldCollect(Ljava/lang/Object;Z)Z
    .locals 0
    return p1
.end method

.method private static flowFor(Ljava/lang/Object;)Lkotlinx/coroutines/flow/Flow;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method
