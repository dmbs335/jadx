.class final Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static active()Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static close()V
    .locals 0
    return-void
.end method

.method private static emit(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static parse()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method

.method private static reconnect()Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static shouldReconnect()Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static shouldSkipA(Ljava/lang/Object;)Z
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method private static shouldSkipB(Ljava/lang/Object;)Z
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method private static suspended()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 7

    iget-object v0, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$0:Ljava/lang/Object;
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->suspended()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->label:I
    const/4 v3, 0x0
    const/4 v4, 0x1
    const/4 v5, 0x2
    const/4 v6, 0x3
    if-eqz v2, :initial
    if-eq v2, v4, :resume_parse
    if-eq v2, v5, :resume_emit
    if-eq v2, v6, :resume_reconnect
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_emit
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->throwOnFailure(Ljava/lang/Object;)V
    goto :inner_header

    :resume_parse
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->throwOnFailure(Ljava/lang/Object;)V
    goto :parse_result

    :resume_reconnect
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->throwOnFailure(Ljava/lang/Object;)V
    goto :outer_header

    :initial
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->throwOnFailure(Ljava/lang/Object;)V

    :outer_header
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->active()Z
    move-result v2
    if-eqz v2, :done

    :inner_header
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->active()Z
    move-result v2
    if-eqz v2, :after_inner
    iput-object v0, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$0:Ljava/lang/Object;
    iput-object v3, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$1:Ljava/lang/Object;
    iput v4, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->label:I
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->parse()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended_return

    :parse_result
    move-object v2, p1

    :event_result
    if-eqz v2, :after_inner
    invoke-static {v2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->shouldSkipA(Ljava/lang/Object;)Z
    move-result p1
    if-nez p1, :inner_header
    invoke-static {v2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->shouldSkipB(Ljava/lang/Object;)Z
    move-result p1
    if-nez p1, :inner_header
    iput-object v0, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$0:Ljava/lang/Object;
    iput-object v2, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$1:Ljava/lang/Object;
    iput v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->label:I
    invoke-static {v2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->emit(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended_return
    goto :inner_header

    :after_inner
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->shouldReconnect()Z
    move-result v2
    if-eqz v2, :close_path
    iput-object v0, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$0:Ljava/lang/Object;
    iput-object v3, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->L$1:Ljava/lang/Object;
    iput v6, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->label:I
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->reconnect()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended_return
    goto :outer_header

    :close_path
    invoke-static {}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$_incoming$1;->close()V
    goto :outer_header

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :suspended_return
    return-object v1
.end method
