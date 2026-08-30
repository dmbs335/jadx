.class public final Lloops/TestCoroutineIteratorTerminalSuspendLoop;
.super Ljava/lang/Object;

.field private $this_map:Ljava/lang/Object;
.field private $transform:Ljava/lang/Object;
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next()Ljava/lang/Object;
    .locals 1
    const-string v0, "element"
    return-object v0
.end method

.method private static transform(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 7

    invoke-static {}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1

    if-eqz v1, :initial
    if-eq v1, v4, :resume_has_next
    if-eq v1, v3, :resume_transform
    if-ne v1, v2, :bad_state

    :resume_send
    iget-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :iterator_loop

    :resume_transform
    iget-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :send_value

    :resume_has_next
    iget-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string v5, "iterator"
    const-string v6, "scope"

    :iterator_loop
    iput-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    const/4 p1, 0x0
    iput-object p1, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$2:Ljava/lang/Object;
    iput v4, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :has_next_result
    goto :suspended

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done

    invoke-static {}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->next()Ljava/lang/Object;
    move-result-object p1
    iput-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    iput-object p1, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$2:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->label:I
    invoke-static {p1, p2}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->transform(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :send_value
    goto :suspended

    :send_value
    iput-object v6, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$1:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->L$2:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->label:I
    invoke-static {p1, p2}, Lloops/TestCoroutineIteratorTerminalSuspendLoop;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    move-object v1, v5
    move-object v5, v1
    goto :iterator_loop

    :suspended
    return-object v0

    :done
    const-string p1, "done"
    return-object p1
.end method
