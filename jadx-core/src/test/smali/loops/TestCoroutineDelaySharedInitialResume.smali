.class public Lloops/TestCoroutineDelaySharedInitialResume;
.super Ljava/lang/Object;

.field private a:I

.method private static action(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static consume(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 5

    invoke-static {}, Lloops/TestCoroutineDelaySharedInitialResume;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineDelaySharedInitialResume;->a:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v1, :shared_initial_resume
    if-eq v1, v4, :resume_receive
    if-eq v1, v3, :resume_action
    if-ne v1, v2, :bad_state
    goto :shared_initial_resume

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :resume_action
    invoke-static {p1}, Lloops/TestCoroutineDelaySharedInitialResume;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_action

    :resume_receive
    invoke-static {p1}, Lloops/TestCoroutineDelaySharedInitialResume;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_receive

    :shared_initial_resume
    invoke-static {p1}, Lloops/TestCoroutineDelaySharedInitialResume;->throwOnFailure(Ljava/lang/Object;)V

    :body
    iput v4, p0, Lloops/TestCoroutineDelaySharedInitialResume;->a:I
    invoke-static {p0}, Lloops/TestCoroutineDelaySharedInitialResume;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_receive
    invoke-static {p1}, Lloops/TestCoroutineDelaySharedInitialResume;->consume(Ljava/lang/Object;)V
    iput v3, p0, Lloops/TestCoroutineDelaySharedInitialResume;->a:I
    invoke-static {p0}, Lloops/TestCoroutineDelaySharedInitialResume;->action(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_action
    iput v2, p0, Lloops/TestCoroutineDelaySharedInitialResume;->a:I
    invoke-static {p0}, Lloops/TestCoroutineDelaySharedInitialResume;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :body

    :suspended
    return-object v0
.end method
