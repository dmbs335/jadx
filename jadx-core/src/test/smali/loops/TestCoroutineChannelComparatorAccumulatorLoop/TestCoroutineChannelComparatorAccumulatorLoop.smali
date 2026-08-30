.class public final Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;
.super Ljava/lang/Object;

.method public static final maxWith(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/util/Comparator;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 13

    instance-of v0, p2, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;
    iget v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->label:I
    const/high16 v5, -0x80000000
    and-int v6, v3, v5
    if-eqz v6, :new_state
    sub-int/2addr v3, v5
    iput v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;
    invoke-direct {v0, p2}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v4, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->label:I
    const/4 v5, 0x1
    if-eqz v3, :initial
    if-ne v3, v5, :bad_state
    iget-object v7, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->L$1:Ljava/lang/Object;
    iget-object p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->L$0:Ljava/lang/Object;
    check-cast p1, Ljava/util/Comparator;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_bridge

    :resume_bridge
    move-object v8, v0
    move-object v0, p0
    move-object p0, v8
    goto :value_join

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const-string v7, "seed"

    :loop
    iput-object p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->L$0:Ljava/lang/Object;
    iput-object v7, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->L$1:Ljava/lang/Object;
    iput v5, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$maxWith$1;->label:I
    invoke-static {v0}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v1, :direct_result
    return-object v1

    :direct_result
    move-object v8, v0
    move-object v0, p0
    move-object p0, v8

    :value_join
    move-object v0, v8
    goto :projection

    :projection
    check-cast v4, Ljava/lang/Boolean;
    invoke-virtual {v4}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2
    if-eqz v2, :exhausted
    invoke-static {}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->next()Ljava/lang/Object;
    move-result-object v9
    move-object v10, v7
    invoke-interface {p1, v7, v9}, Ljava/util/Comparator;->compare(Ljava/lang/Object;Ljava/lang/Object;)I
    move-result v2
    if-gez v2, :selected
    move-object v10, v9

    :selected
    move-object v7, v10
    goto :loop

    :exhausted
    return-object v7
.end method

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next()Ljava/lang/Object;
    .registers 1
    const-string v0, "item"
    return-object v0
.end method
