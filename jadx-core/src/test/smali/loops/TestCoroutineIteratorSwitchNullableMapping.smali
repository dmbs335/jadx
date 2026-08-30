.class public Lloops/TestCoroutineIteratorSwitchNullableMapping;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private items:Ljava/lang/Iterable;
.field private label:I

.method private static b(Ljava/lang/Object;)V
    .locals 0

    return-void
.end method

.method private itemsAsync(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1

    iget-object v0, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->items:Ljava/lang/Iterable;
    return-object v0
.end method

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

    iget-object v11, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$2:Ljava/lang/Object;
    check-cast v11, Ljava/util/List;
    sget-object v1, Lkotlin/coroutines/intrinsics/CoroutineSingletons;->COROUTINE_SUSPENDED:Lkotlin/coroutines/intrinsics/CoroutineSingletons;
    iget v2, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->label:I
    if-eqz v2, :initial
    const/4 v6, 0x3
    if-eq v2, v6, :resume_b
    const/4 v6, 0x2
    if-eq v2, v6, :resume_a
    const/4 v6, 0x1
    if-eq v2, v6, :resume_items

    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v2, "bad state"
    invoke-direct {v0, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :resume_a
    iget-object v3, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$0:Ljava/lang/Object;
    check-cast v3, Ljava/util/Collection;
    iget-object v4, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->b(Ljava/lang/Object;)V
    move-object v5, p1
    goto :map_a_join

    :resume_b
    iget-object v3, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$0:Ljava/lang/Object;
    check-cast v3, Ljava/util/Collection;
    iget-object v4, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$1:Ljava/lang/Object;
    check-cast v4, Ljava/util/Iterator;
    invoke-static {p1}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->b(Ljava/lang/Object;)V
    move-object v5, p1
    goto :map_b_join

    :resume_items
    invoke-static {p1}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->b(Ljava/lang/Object;)V
    move-object v0, p1
    goto :items_join

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->b(Ljava/lang/Object;)V
    const/4 v6, 0x1
    iput v6, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->itemsAsync(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, v1, :items_join
    return-object v1

    :items_join
    check-cast v0, Ljava/lang/Iterable;
    new-instance v3, Ljava/util/ArrayList;
    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V
    invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v4

    :loop_header
    const/4 v5, 0x0
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Ljava/lang/Integer;
    invoke-virtual {v7}, Ljava/lang/Integer;->intValue()I
    move-result v7
    packed-switch v7, :switch_data

    :nullable
    if-eqz v5, :loop_state
    invoke-interface {v3, v5}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    move-result v10

    :loop_state
    const/4 v10, 0x4
    goto :loop_header

    :case_0
    const-string v5, "zero"
    goto :nullable

    :case_1
    const-string v5, "one"
    goto :nullable

    :case_2
    const-string v5, "two"
    goto :nullable

    :case_3
    const-string v5, "three"
    goto :nullable

    :case_4
    const-string v5, "four"
    goto :nullable

    :case_5
    const-string v5, "five"
    goto :nullable

    :case_6
    const-string v5, "six"
    goto :nullable

    :case_7
    const-string v5, "seven"
    goto :nullable

    :case_8
    iput-object v3, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$1:Ljava/lang/Object;
    const/4 v6, 0x2
    iput v6, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->mapA(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :map_a_join
    return-object v1

    :map_a_join
    check-cast v5, Ljava/lang/String;
    goto :nullable

    :case_9
    iput-object v3, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->L$1:Ljava/lang/Object;
    const/4 v6, 0x3
    iput v6, p0, Lloops/TestCoroutineIteratorSwitchNullableMapping;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineIteratorSwitchNullableMapping;->mapB(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :map_b_join
    return-object v1

    :map_b_join
    check-cast v5, Ljava/lang/String;
    goto :nullable

    :done
    return-object v3

    :switch_data
    .packed-switch 0x0
        :case_0
        :case_1
        :case_2
        :case_3
        :case_4
        :case_5
        :case_6
        :case_7
        :case_8
        :case_9
    .end packed-switch
.end method
