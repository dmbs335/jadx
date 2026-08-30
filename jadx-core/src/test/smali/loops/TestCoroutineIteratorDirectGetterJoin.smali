.class public Lloops/TestCoroutineIteratorDirectGetterJoin;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I
.field private result:Ljava/lang/Object;

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 8

    iget-object p1, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->result:Ljava/lang/Object;
    iget-object v0, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->L$0:Ljava/lang/Object;
    invoke-static {}, Lloops/TestCoroutineIteratorDirectGetterJoin;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->label:I
    const/4 v3, 0x1
    const/4 v5, 0x0
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v4, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lloops/TestCoroutineIteratorDirectGetterJoin;->throwOnFailure(Ljava/lang/Object;)V
    goto :result

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorDirectGetterJoin;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p1
    check-cast v4, Ljava/util/Iterator;

    :loop
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v6
    iput-object v0, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->L$1:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineIteratorDirectGetterJoin;->label:I
    invoke-static {v6, p2}, Lloops/TestCoroutineIteratorDirectGetterJoin;->load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-eq v5, v1, :suspended

    move-object v7, v4
    move-object v4, v7
    move-object p1, v5
    const/4 v6, 0x0

    :result
    check-cast p1, Ljava/io/File;
    invoke-virtual {p1}, Ljava/io/File;->getParent()Ljava/lang/String;
    move-result-object p1
    if-eqz p1, :loop

    :done
    return-object p1

    :suspended
    return-object v1
.end method
