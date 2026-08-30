.class public final Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private static final q:Ljava/lang/Object;
.field private static final u:Ljava/lang/Object;

.field private s:I
.field private t:I

.method static constructor <clinit>()V
    .registers 1

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->q:Ljava/lang/Object;
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->u:Ljava/lang/Object;
    return-void
.end method

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    iget v0, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->s:I
    packed-switch v0, :selector_data
    goto :target_dispatch

    :simple_dispatch
    sget-object v0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->q:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    const/4 v2, 0x1
    if-eqz v1, :simple_initial
    if-ne v1, v2, :simple_bad_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :simple_done

    :simple_bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :simple_initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iput v2, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->otherSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :simple_suspended

    :simple_done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :simple_suspended
    return-object v0

    :other_dispatch
    sget-object v0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->q:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    const/4 v2, 0x1
    if-eqz v1, :other_initial
    if-ne v1, v2, :other_bad_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :other_done

    :other_bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :other_initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_start
    invoke-static {}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->sideEffect()V
    :try_end
    .catchall {:try_start .. :try_end} :other_handler
    iput v2, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->otherSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :other_suspended

    :other_done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :other_handler
    move-exception v7
    throw v7

    :other_suspended
    return-object v0

    :target_dispatch
    sget-object v0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->q:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3
    const/4 v5, 0x4
    if-eqz v1, :target_initial
    if-eq v1, v2, :resume_refresh
    if-eq v1, v3, :resume_fetch
    if-eq v1, v4, :resume_publish
    if-ne v1, v5, :target_bad_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :refresh_call

    :resume_publish
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :delay_call

    :resume_fetch
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :consume_result

    :resume_refresh
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :fetch_call

    :target_bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :target_initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :refresh_call
    iput v2, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->refresh(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :target_suspended

    :fetch_call
    iput v3, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->fetch(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :target_suspended

    :consume_result
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->consume(Ljava/lang/Object;)V
    iput v4, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p1, p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->publish(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    sget-object p1, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->u:Ljava/lang/Object;
    if-eq p1, v0, :target_suspended

    :delay_call
    iput v5, p0, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->t:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedOuterSwitchCompleteStateMap;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :refresh_call

    :target_suspended
    return-object v0

    :selector_data
    .packed-switch 0x0
        :other_dispatch
        :simple_dispatch
    .end packed-switch
.end method

.method private static refresh(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static fetch(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static publish(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static otherSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static consume(Ljava/lang/Object;)V
    .registers 1

    return-void
.end method

.method private static sideEffect()V
    .registers 0

    return-void
.end method
