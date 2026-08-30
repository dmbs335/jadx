.class public final Lloops/TestCoroutineIteratorMapPutResumeTail;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private label:I
.field private result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    iput-object p1, p0, Lloops/TestCoroutineIteratorMapPutResumeTail;->result:Ljava/lang/Object;
    iget p1, p0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, p0}, Lloops/TestCoroutineIteratorMapPutResumeTail;->store(Ljava/util/List;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method

.method public static store(Ljava/util/List;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p2, Lloops/TestCoroutineIteratorMapPutResumeTail;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lloops/TestCoroutineIteratorMapPutResumeTail;
    iget v1, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineIteratorMapPutResumeTail;
    invoke-direct {v0, p2}, Lloops/TestCoroutineIteratorMapPutResumeTail;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object p2, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object p0, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$0:Ljava/lang/Object;
    check-cast p0, Ljava/util/List;
    iget-object p1, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$1:Ljava/lang/Object;
    check-cast p1, Ljava/util/Map;
    iget-object v4, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$2:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    iget-object v5, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$3:Ljava/lang/Object;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :result_join

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke'"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v4

    :loop
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v5
    if-eqz v5, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v5

    iput-object p0, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$2:Ljava/lang/Object;
    iput-object v5, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->L$3:Ljava/lang/Object;
    iput v3, v0, Lloops/TestCoroutineIteratorMapPutResumeTail;->label:I
    invoke-static {v5, v0}, Lloops/TestCoroutineIteratorMapPutResumeTail;->insert(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v6
    if-ne v6, v1, :direct_complete
    return-object v1

    :direct_complete
    move-object p2, v6
    move-object v7, p0
    move-object p0, v7
    move-object v7, p1
    move-object p1, v7

    :result_join
    invoke-interface {p1, v5, p2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-object v7, p0
    move-object p0, v7
    move-object v7, p1
    move-object p1, v7
    move-object v7, v4
    move-object v4, v7
    move-object v7, v0
    move-object v0, v7
    move-object v7, v1
    move-object v1, v7
    move v8, v3
    move v3, v8
    goto :loop

    :done
    return-object p1
.end method

.method private static insert(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method
