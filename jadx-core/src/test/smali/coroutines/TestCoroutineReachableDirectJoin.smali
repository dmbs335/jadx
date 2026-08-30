.class public Lcoroutines/TestCoroutineReachableDirectJoin;
.super Ljava/lang/Object;

.method private static step()V
    .registers 0
    return-void
.end method

.method private static tail()V
    .registers 0
    return-void
.end method

.method private static emit(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method public static run(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3

    if-eqz p0, :loop
    goto :shared_tail

    :loop
    if-eqz p0, :shared_tail
    invoke-static {}, Lcoroutines/TestCoroutineReachableDirectJoin;->step()V
    const/4 p0, 0x0
    goto :loop

    :shared_tail
    invoke-static {}, Lcoroutines/TestCoroutineReachableDirectJoin;->tail()V
    invoke-static {p1}, Lcoroutines/TestCoroutineReachableDirectJoin;->emit(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object p0
    if-ne v0, p0, :complete
    return-object p0

    :complete
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method
