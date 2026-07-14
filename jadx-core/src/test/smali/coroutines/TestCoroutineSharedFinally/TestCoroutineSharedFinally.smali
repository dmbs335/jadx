.class public Lcoroutines/TestCoroutineSharedFinally;
.super Ljava/lang/Object;

.method private static suspendCall(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static use(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static cleanup()V
    .locals 0
    return-void
.end method

.method public static run(Ljava/lang/Object;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 6

    instance-of v0, p2, Lcoroutines/TestCoroutineSharedFinally$Continuation;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;
    iget v1, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;
    invoke-direct {v0, p2}, Lcoroutines/TestCoroutineSharedFinally$Continuation;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->result:Ljava/lang/Object;
    iget v2, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->label:I
    const/4 v3, 0x1
    const/4 v4, 0x2
    if-eqz v2, :state_0
    if-eq v2, v3, :state_1
    if-ne v2, v4, :bad_state
    iget-object p0, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->L$0:Ljava/lang/Object;
    goto :render

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    invoke-direct {p0}, Ljava/lang/IllegalStateException;-><init>()V
    throw p0

    :state_1
    iget-object p0, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->L$0:Ljava/lang/Object;
    goto :frame

    :state_0
    if-eqz p1, :call_second
    invoke-static {p0, v0}, Lcoroutines/TestCoroutineSharedFinally;->suspendCall(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, p0, :after_scroll
    goto :suspended

    :call_second
    invoke-static {p0, v0}, Lcoroutines/TestCoroutineSharedFinally;->suspendCall(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, p0, :after_scroll

    :suspended
    return-object p0

    :after_scroll
    iput-object p0, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->L$0:Ljava/lang/Object;
    iput v3, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->label:I

    :frame
    iput-object p0, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->L$0:Ljava/lang/Object;
    iput v4, v0, Lcoroutines/TestCoroutineSharedFinally$Continuation;->label:I
    invoke-static {p0, v0}, Lcoroutines/TestCoroutineSharedFinally;->suspendCall(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, p0, :suspended

    :render
    :try_start
    invoke-static {p0}, Lcoroutines/TestCoroutineSharedFinally;->use(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    :try_end
    invoke-static {}, Lcoroutines/TestCoroutineSharedFinally;->cleanup()V
    return-object v1

    :catchall
    move-exception v5
    invoke-static {}, Lcoroutines/TestCoroutineSharedFinally;->cleanup()V
    throw v5

    .catchall {:try_start .. :try_end} :catchall
.end method
