.class public Lloops/TestCoroutineTwoSuspendBooleanLoop;
.super Ljava/lang/Object;

.field private iterator:Ljava/lang/Object;
.field private label:I
.field private sink:Ljava/lang/Object;

.method private static hasNext(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static send(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 7

    iget v0, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_has_next
    const/4 v1, 0x2
    if-eq v0, v1, :resume_send
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_send
    iget-object v1, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->iterator:Ljava/lang/Object;
    iget-object v2, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->sink:Ljava/lang/Object;
    goto :resume_send_tail

    :resume_send_tail
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendBooleanLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_send

    :resume_has_next
    iget-object v1, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->iterator:Ljava/lang/Object;
    iget-object v2, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->sink:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendBooleanLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v3, p1
    goto :has_result

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendBooleanLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v1, p0
    move-object v2, p0

    :loop_header
    iput-object v1, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->iterator:Ljava/lang/Object;
    iput-object v2, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->sink:Ljava/lang/Object;
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->label:I
    invoke-static {v1}, Lloops/TestCoroutineTwoSuspendBooleanLoop;->hasNext(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, p2, :suspended
    move-object v3, v4

    :has_result
    check-cast v3, Ljava/lang/Boolean;
    invoke-virtual {v3}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v5
    if-eqz v5, :done_prep

    iput-object v1, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->iterator:Ljava/lang/Object;
    iput-object v2, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->sink:Ljava/lang/Object;
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCoroutineTwoSuspendBooleanLoop;->label:I
    invoke-static {v2}, Lloops/TestCoroutineTwoSuspendBooleanLoop;->send(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, p2, :suspended

    :after_send
    move-object v6, v2
    move-object v2, v6
    goto :loop_header

    :done_prep
    move-object v6, v1
    goto :done_bridge

    :done_bridge
    move-object v1, v6
    goto :done

    :done
    return-object p1

    :suspended
    return-object p2

.end method
