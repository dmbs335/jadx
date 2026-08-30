.class public Lloops/TestCoroutineDelayObfuscatedLabel;
.super Ljava/lang/Object;

.field private a:I
.field private runningDelay:I
.field private savedIndex:I
.field private savedLimit:I
.field private scope:Ljava/lang/Object;

.method private static consume()V
    .locals 0
    return-void
.end method

.method private static consumeScope(Lkotlinx/coroutines/CoroutineScope;)V
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

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 5

    iget-object v4, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->scope:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/CoroutineScope;
    invoke-static {}, Lloops/TestCoroutineDelayObfuscatedLabel;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->a:I
    const/4 v2, 0x2
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    invoke-static {p1}, Lloops/TestCoroutineDelayObfuscatedLabel;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_delay

    :bad_state
    new-instance v3, Ljava/lang/IllegalStateException;
    invoke-direct {v3}, Ljava/lang/IllegalStateException;-><init>()V
    throw v3

    :initial
    invoke-static {p1}, Lloops/TestCoroutineDelayObfuscatedLabel;->throwOnFailure(Ljava/lang/Object;)V

    :body
    invoke-static {v4}, Lloops/TestCoroutineDelayObfuscatedLabel;->consumeScope(Lkotlinx/coroutines/CoroutineScope;)V
    invoke-static {}, Lloops/TestCoroutineDelayObfuscatedLabel;->consume()V
    iput v1, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->savedIndex:I
    const/4 v3, 0x3
    iput v3, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->savedLimit:I
    iput v2, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->a:I
    invoke-static {p0}, Lloops/TestCoroutineDelayObfuscatedLabel;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended

    :after_delay
    iget v4, p0, Lloops/TestCoroutineDelayObfuscatedLabel;->runningDelay:I
    if-lez v4, :done
    goto :body

    :suspended
    return-object v0

    :done
    sget-object v4, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v4
.end method
