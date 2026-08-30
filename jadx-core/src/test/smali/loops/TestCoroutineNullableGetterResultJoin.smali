.class public Lloops/TestCoroutineNullableGetterResultJoin;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

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
    .locals 7

    iget-object v0, p0, Lloops/TestCoroutineNullableGetterResultJoin;->L$0:Ljava/lang/Object;
    check-cast v0, Lkotlinx/coroutines/CoroutineScope;
    invoke-static {}, Lloops/TestCoroutineNullableGetterResultJoin;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineNullableGetterResultJoin;->label:I
    const/4 v3, 0x1
    const/4 v5, 0x0
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v4, p0, Lloops/TestCoroutineNullableGetterResultJoin;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lloops/TestCoroutineNullableGetterResultJoin;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :result

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :initial
    invoke-static {p1}, Lloops/TestCoroutineNullableGetterResultJoin;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p1
    check-cast v4, Ljava/util/Iterator;

    :loop
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v6
    iput-object v0, p0, Lloops/TestCoroutineNullableGetterResultJoin;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineNullableGetterResultJoin;->L$1:Ljava/lang/Object;
    const/4 v3, 0x1
    iput v3, p0, Lloops/TestCoroutineNullableGetterResultJoin;->label:I
    invoke-static {v6, p2}, Lloops/TestCoroutineNullableGetterResultJoin;->load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-eq v5, v1, :suspended

    :result
    check-cast v5, Ljava/io/File;
    invoke-virtual {v5}, Ljava/io/File;->getParent()Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :loop

    :done
    return-object v5

    :suspended
    return-object v1
.end method
