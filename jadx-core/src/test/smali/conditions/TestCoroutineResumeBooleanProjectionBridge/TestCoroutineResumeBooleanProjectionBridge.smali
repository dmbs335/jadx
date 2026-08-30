.class public final Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;
.super Ljava/lang/Object;

.method public static final toCollection(Ljava/util/Collection;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    instance-of v0, p1, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;
    if-eqz v0, :new_state
    move-object v0, p1
    check-cast v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;
    iget v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->label:I
    const/high16 v3, -0x80000000
    and-int v5, v2, v3
    if-eqz v5, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;
    invoke-direct {v0, p1}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state
    iget-object v4, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->L$0:Ljava/lang/Object;
    check-cast v4, Ljava/util/Collection;
    goto :resume

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p0
    goto :loop

    :resume
    :try_start
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :projection

    :loop
    iput-object v4, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$toCollection$1;->label:I
    invoke-static {v0}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :direct_result
    return-object v1

    :direct_result
    move-object v5, p1
    move-object p1, v5

    :projection
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2
    if-eqz v2, :done
    const-string v5, "item"
    invoke-interface {v4, v5}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    goto :loop

    :done
    return-object v4
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

	:catch_all
	move-exception v5
	invoke-static {v5}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->onError(Ljava/lang/Throwable;)V
	throw v5
.end method

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static native onError(Ljava/lang/Throwable;)V
.end method
