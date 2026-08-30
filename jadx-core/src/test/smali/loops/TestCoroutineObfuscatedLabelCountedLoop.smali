.class public final Lloops/TestCoroutineObfuscatedLabelCountedLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private static final q:Ljava/lang/Object;

.field private a:I
.field private b:I

.method static constructor <clinit>()V
    .registers 1

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->q:Ljava/lang/Object;
    return-void
.end method

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    sget-object v0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->q:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->b:I
    const-string v2, "progress"
    const/4 v3, 0x3
    const/4 v4, 0x1

    if-eqz v1, :initial
    if-ne v1, v4, :bad_state

    iget v1, p0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->a:I
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->f(Ljava/lang/Object;)V
    goto :post_suspend

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->f(Ljava/lang/Object;)V
    const/4 v1, 0x1

    :loop
    iput v1, p0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->a:I
    iput v4, p0, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->b:I
    invoke-static {v1, p0}, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->k(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :post_suspend
    rem-int/lit16 v5, v1, 0x3e8
    if-nez v5, :skip_progress
    invoke-static {v2, v1}, Lloops/TestCoroutineObfuscatedLabelCountedLoop;->e(Ljava/lang/String;I)V

    :skip_progress
    if-eq v1, v3, :done
    add-int/lit8 v1, v1, 0x1
    goto :loop

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

.method private static k(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static e(Ljava/lang/String;I)V
    .registers 2

    return-void
.end method
