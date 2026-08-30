.class public final Lloops/TestCoroutineInlinedComparatorIteratorLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field L$3:Ljava/lang/Object;
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
    .locals 10

    iput-object p1, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->result:Ljava/lang/Object;
    invoke-static {}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->suspended()Ljava/lang/Object;
    move-result-object v0
    iget-object p1, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->result:Ljava/lang/Object;
    iget v1, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->label:I
    if-eqz v1, :initial
    const/4 v2, 0x1
    if-eq v1, v2, :resume_one
    const/4 v2, 0x2
    if-eq v1, v2, :resume_two

    new-instance v9, Ljava/lang/IllegalStateException;
    invoke-direct {v9}, Ljava/lang/IllegalStateException;-><init>()V
    throw v9

    :resume_two
    iget-object v3, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$3:Ljava/lang/Object;
    iget-object v4, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$2:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$0:Ljava/lang/Object;
    check-cast v6, Ljava/util/Comparator;
    invoke-static {p1}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->b(Ljava/lang/Object;)V
    goto :result

    :resume_one
    iget-object v4, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$2:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$0:Ljava/lang/Object;
    check-cast v6, Ljava/util/Comparator;
    invoke-static {p1}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->b(Ljava/lang/Object;)V
    const-string v3, "seed"
    goto :loop

    :initial
    invoke-static {p1}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->b(Ljava/lang/Object;)V
    const/4 v6, 0x0
    const-string v5, "channel"
    const-string v4, "iterator"
    const-string v3, "seed"

    :loop
    iput-object v6, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$1:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$2:Ljava/lang/Object;
    iput-object v3, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->L$3:Ljava/lang/Object;
    const/4 v2, 0x2
    iput v2, p0, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :result
    return-object v0

    :result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v7
    if-eqz v7, :done
    invoke-static {}, Lloops/TestCoroutineInlinedComparatorIteratorLoop;->c()Ljava/lang/Object;
    move-result-object v8
    invoke-interface {v6, v3, v8}, Ljava/util/Comparator;->compare(Ljava/lang/Object;Ljava/lang/Object;)I
    move-result v7
    if-lez v7, :keep
    move-object v3, v8

    :keep
    goto :loop

    :done
    return-object v3

.end method
