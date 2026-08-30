.class public final Lloops/TestCoroutineInlinedTerminalIteratorLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I
.field synthetic result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static suspended()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static c()Ljava/lang/Object;
    .locals 1
    const-string v0, "next"
    return-object v0
.end method

.method private static b(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 8

    iput-object p1, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->result:Ljava/lang/Object;
    invoke-static {}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->suspended()Ljava/lang/Object;
    move-result-object v0
    iget-object p1, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->result:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->label:I
    if-eqz v1, :initial
    const/4 v2, 0x1
    if-eq v1, v2, :resume_one
    const/4 v2, 0x2
    if-eq v1, v2, :resume_two

    new-instance v7, Ljava/lang/IllegalStateException;
    invoke-direct {v7}, Ljava/lang/IllegalStateException;-><init>()V
    throw v7

    :resume_two
    iget-object v4, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$2:Ljava/lang/Object;
    iget-object v3, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$1:Ljava/lang/Object;
    iget-object v2, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->b(Ljava/lang/Object;)V
    goto :result

    :resume_one
    iget-object v3, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$1:Ljava/lang/Object;
    iget-object v2, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->b(Ljava/lang/Object;)V
    const-string v4, "seed"
    goto :loop

    :initial
    invoke-static {p1}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->b(Ljava/lang/Object;)V
    const-string v2, "channel"
    const-string v3, "iterator"
    const-string v4, "seed"

    :loop
    iput-object v2, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$0:Ljava/lang/Object;
    iput-object v3, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$1:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->L$2:Ljava/lang/Object;
    const/4 v5, 0x2
    iput v5, p0, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :result
    return-object v0

    :result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v6
    if-eqz v6, :done
    invoke-static {}, Lloops/TestCoroutineInlinedTerminalIteratorLoop;->c()Ljava/lang/Object;
    move-result-object v4
    goto :loop

    :done
    return-object v4
.end method
