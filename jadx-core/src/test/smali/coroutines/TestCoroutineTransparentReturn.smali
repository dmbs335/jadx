.class public Lcoroutines/TestCoroutineTransparentReturn;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.method private static call(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method public invokeSuspend(ILjava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 2

    packed-switch p1, :switch_data
    goto :bad_state

    :state_0
    invoke-static {p3}, Lcoroutines/TestCoroutineTransparentReturn;->call(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :normal_return
    const/4 v1, 0x0
    goto :shared_suspend

    :state_1
    const/4 v1, 0x1

    :shared_suspend
    if-eqz v1, :suspend_value
    move-object v0, p2
    goto :suspend_return

    :suspend_value
    move-object v0, p2

    :suspend_return
    return-object v0

    :normal_return
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :switch_data
    .packed-switch 0x0
        :state_0
        :state_1
    .end packed-switch
.end method
