.class public final Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private static final q:Ljava/lang/Object;

.field private a:I
.field private b:I
.field private c:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 1

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->q:Ljava/lang/Object;
    return-void
.end method

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    sget-object v0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->q:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->b:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v6, 0x3

    if-eqz v1, :initial
    if-eq v1, v2, :resume_first
    if-ne v1, v3, :bad_state

    iget v1, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->a:I
    iget-object v4, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->c:Ljava/lang/String;
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->f(Ljava/lang/Object;)V
    goto :post_second

    :resume_first
    iget v1, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->a:I
    iget-object v4, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->c:Ljava/lang/String;
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->f(Ljava/lang/Object;)V
    goto :post_first

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->f(Ljava/lang/Object;)V
    const-string v4, "suffix"
    const/4 v1, 0x1

    :loop
    iput v1, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->a:I
    iput-object v4, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->c:Ljava/lang/String;
    iput v2, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->b:I
    invoke-static {v1, v4, p0}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->firstSuspend(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    move-object v5, p1
    move-object p1, v5
    goto :post_first

    :post_first
    :try_start
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->write(Ljava/lang/Object;)V
    :try_end
    .catchall {:try_start .. :try_end} :handler

    iput v1, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->a:I
    iput-object v4, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->c:Ljava/lang/String;
    iput v3, p0, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->b:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->secondSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :post_second
    invoke-static {v1}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->progress(I)V
    if-eq v1, v6, :done
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :handler
    move-exception v7
    invoke-static {v7}, Lloops/TestCoroutineObfuscatedTwoStateLocalTryLoop;->onError(Ljava/lang/Throwable;)V
    throw v7

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method

.method private static f(Ljava/lang/Object;)V
    .registers 1

    return-void
.end method

.method private static firstSuspend(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static secondSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static write(Ljava/lang/Object;)V
    .registers 1

    return-void
.end method

.method private static progress(I)V
    .registers 1

    return-void
.end method

.method private static onError(Ljava/lang/Throwable;)V
    .registers 1

    return-void
.end method
