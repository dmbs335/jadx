.class public final Lloops/TestCoroutineObfuscatedStateResultLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private a:Ljava/lang/Object;
.field private b:I
.field private e:Ljava/lang/Object;
.field private f:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Lloops/TestCoroutineObfuscatedStateResultLoop;->a:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    invoke-static {p0}, Lloops/TestCoroutineObfuscatedStateResultLoop;->run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    instance-of v0, p0, Lloops/TestCoroutineObfuscatedStateResultLoop;
    if-eqz v0, :new_continuation
    move-object v0, p0
    check-cast v0, Lloops/TestCoroutineObfuscatedStateResultLoop;
    iget v1, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineObfuscatedStateResultLoop;
    invoke-direct {v0, p0}, Lloops/TestCoroutineObfuscatedStateResultLoop;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->a:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    if-eqz v3, :initial
    const/4 v4, 0x1
    if-ne v3, v4, :bad_state
    iget-object v5, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->e:Ljava/lang/Object;
    check-cast v5, Ljava/util/Iterator;
    iget-object v6, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->f:Ljava/lang/Object;
    check-cast v6, Ljava/io/Closeable;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :result_body

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestCoroutineObfuscatedStateResultLoop;->items()Ljava/util/Iterator;
    move-result-object v5

    :loop
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
    move-result v3
    if-eqz v3, :not_found
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v6
    check-cast v6, Ljava/io/Closeable;
    iput-object v5, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->e:Ljava/lang/Object;
    iput-object v6, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->f:Ljava/lang/Object;
    const/4 v3, 0x1
    iput v3, v0, Lloops/TestCoroutineObfuscatedStateResultLoop;->b:I
    invoke-static {v6, v0}, Lloops/TestCoroutineObfuscatedStateResultLoop;->load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended

    :result_body
    const/4 v4, 0x0
    invoke-static {v6, v4}, Lkotlin/io/CloseableKt;->closeFinally(Ljava/io/Closeable;Ljava/lang/Throwable;)V
    invoke-static {v1}, Lloops/TestCoroutineObfuscatedStateResultLoop;->accept(Ljava/lang/Object;)Z
    move-result v3
    if-eqz v3, :loop
    return-object v1

    :not_found
    const/4 v1, 0x0
    return-object v1

    :suspended
    return-object v2
.end method

.method private static items()Ljava/util/Iterator;
    .registers 1

    const/4 v0, 0x0
    return-object v0
.end method

.method private static load(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3

    return-object p0
.end method

.method private static accept(Ljava/lang/Object;)Z
    .registers 2

    const/4 v0, 0x0
    return v0
.end method
