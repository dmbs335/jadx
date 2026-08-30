.class public final Lloops/TestCoroutineIteratorCollectionMapCompletion;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private I$0:I
.field private I$1:I
.field private I$2:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private L$4:Ljava/lang/Object;
.field private L$5:Ljava/lang/Object;
.field private L$6:Ljava/lang/Object;
.field private L$7:Ljava/lang/Object;
.field private label:I
.field private result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    iput-object p1, p0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->result:Ljava/lang/Object;
    iget p1, p0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, p0}, Lloops/TestCoroutineIteratorCollectionMapCompletion;->map(Ljava/lang/Object;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method

.method public static map(Ljava/lang/Object;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 16

    instance-of v0, p2, Lloops/TestCoroutineIteratorCollectionMapCompletion;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;
    iget v1, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;
    invoke-direct {v0, p2}, Lloops/TestCoroutineIteratorCollectionMapCompletion;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object p2, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    const/4 v3, 0x1
    const/4 v4, 0x0
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget p1, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->I$1:I
    iget v2, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->I$0:I
    iget-object v5, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$7:Ljava/lang/Object;
    check-cast v5, Ljava/util/Collection;
    iget-object v6, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$6:Ljava/lang/Object;
    iget-object v6, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$4:Ljava/lang/Object;
    check-cast v6, Ljava/util/Iterator;
    iget-object v7, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$3:Ljava/lang/Object;
    check-cast v7, Ljava/util/Collection;
    iget-object v8, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$2:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Iterable;
    iget-object v9, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$1:Ljava/lang/Object;
    check-cast v9, Ljava/lang/Iterable;
    iget-object v10, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$0:Ljava/lang/Object;
    check-cast v10, Ljava/util/List;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :result_join

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object p2, p1
    check-cast p2, Ljava/lang/Iterable;
    new-instance v2, Ljava/util/ArrayList;
    const/16 v5, 0xa
    invoke-static {p2, v5}, Lkotlin/collections/CollectionsKt;->collectionSizeOrDefault(Ljava/lang/Iterable;I)I
    move-result v5
    invoke-direct {v2, v5}, Ljava/util/ArrayList;-><init>(I)V
    invoke-interface {p2}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v5
    move-object v8, p2
    move-object v9, v8
    move p2, v4
    move-object v6, v5
    move-object v5, v2
    move v2, p2

    :loop
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z
    move-result v7
    if-eqz v7, :done
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    move-object v10, v7
    invoke-static {v10}, Lloops/TestCoroutineIteratorCollectionMapCompletion;->accept(Ljava/lang/Object;)Z
    move-result v11
    if-eqz v11, :add_null

    invoke-static {p1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v11
    iput-object v11, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$0:Ljava/lang/Object;
    invoke-static {v9}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v11
    iput-object v11, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$1:Ljava/lang/Object;
    invoke-static {v8}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v11
    iput-object v11, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$2:Ljava/lang/Object;
    iput-object v5, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$3:Ljava/lang/Object;
    iput-object v6, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$4:Ljava/lang/Object;
    invoke-static {v7}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v7
    iput-object v7, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$5:Ljava/lang/Object;
    invoke-static {v10}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v7
    iput-object v7, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$6:Ljava/lang/Object;
    iput-object v5, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->L$7:Ljava/lang/Object;
    iput v2, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->I$0:I
    iput p2, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->I$1:I
    iput v4, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->I$2:I
    iput v3, v0, Lloops/TestCoroutineIteratorCollectionMapCompletion;->label:I
    invoke-static {v10, v0}, Lloops/TestCoroutineIteratorCollectionMapCompletion;->transform(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-ne v7, v1, :direct_complete
    return-object v1

    :direct_complete
    move-object v10, p1
    move p1, p2
    move-object p2, v7
    move-object v7, v5

    :result_join
    move-object v12, v0
    move v0, p1
    move-object p1, v10
    move-object v10, v9
    move-object v9, v8
    move-object v8, v6
    move-object v6, v5
    move v5, v2
    move-object v2, v12
    goto :add_result

    :add_null
    const/4 v7, 0x0
    move-object v10, v9
    move-object v9, v8
    move-object v8, v6
    move-object v6, v5
    move v5, v2
    move-object v2, v0
    move v0, p2
    move-object p2, v7
    move-object v7, v6

    :add_result
    invoke-interface {v6, p2}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    move p2, v0
    move-object v0, v2
    move v2, v5
    move-object v5, v7
    move-object v6, v8
    move-object v8, v9
    move-object v9, v10
    goto :loop

    :done
    check-cast v5, Ljava/util/List;
    return-object v5
.end method

.method private static accept(Ljava/lang/Object;)Z
    .registers 2
    const/4 v0, 0x1
    return v0
.end method

.method private static transform(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method
