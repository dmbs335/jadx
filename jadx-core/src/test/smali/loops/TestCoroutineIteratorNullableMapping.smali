.class public Lloops/TestCoroutineIteratorNullableMapping;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private items:Ljava/lang/Iterable;
.field private label:I

.method private mapA(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1

    const-string v0, "a"
    return-object v0
.end method

.method private mapB(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1

    const-string v0, "b"
    return-object v0
.end method

.method public invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 12

    iget-object v9, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$2:Ljava/lang/Object;
    check-cast v9, Ljava/util/List;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineIteratorNullableMapping;->label:I
    if-eqz v2, :initial
    const/4 v6, 0x1
    if-eq v2, v6, :resume_a
    const/4 v6, 0x2
    if-eq v2, v6, :resume_b

    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v2, "bad state"
    invoke-direct {v0, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :resume_a
    iget-object v3, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$0:Ljava/lang/Object;
    check-cast v3, Ljava/util/Collection;
    iget-object v4, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :map_a_join

    :resume_b
    iget-object v3, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$0:Ljava/lang/Object;
    check-cast v3, Ljava/util/Collection;
    iget-object v4, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :map_b_join

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v3, Ljava/util/ArrayList;
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V
    iget-object v0, p0, Lloops/TestCoroutineIteratorNullableMapping;->items:Ljava/lang/Iterable;
    invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v4

    :loop_header
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Ljava/lang/Integer;
    invoke-virtual {v7}, Ljava/lang/Integer;->intValue()I
    move-result v7
    if-nez v7, :check_b

    iput-object v3, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$1:Ljava/lang/Object;
    const/4 v6, 0x1
    iput v6, p0, Lloops/TestCoroutineIteratorNullableMapping;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineIteratorNullableMapping;->mapA(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :map_a_join
    return-object v1

    :map_a_join
    check-cast v5, Ljava/lang/String;
    goto :nullable

    :check_b
    const/4 v8, 0x1
    if-ne v7, v8, :null_value

    iput-object v3, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorNullableMapping;->L$1:Ljava/lang/Object;
    const/4 v6, 0x2
    iput v6, p0, Lloops/TestCoroutineIteratorNullableMapping;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineIteratorNullableMapping;->mapB(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :map_b_join
    return-object v1

    :map_b_join
    check-cast v5, Ljava/lang/String;
    goto :nullable

    :null_value
    const/4 v5, 0x0

    :nullable
    if-eqz v5, :loop_state
    invoke-interface {v3, v5}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    move-result v0

    :loop_state
    const/4 v10, 0x3
    goto :loop_header

    :done
    return-object v3
.end method
