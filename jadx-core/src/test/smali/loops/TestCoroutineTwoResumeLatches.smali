.class public Lloops/TestCoroutineTwoResumeLatches;
.super Ljava/lang/Object;

.field private firstIndex:I
.field private secondIndex:I
.field private label:I

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static suspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCoroutineTwoResumeLatches;->label:I
    packed-switch v0, :states

    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_first
    iget v2, p0, Lloops/TestCoroutineTwoResumeLatches;->firstIndex:I
    invoke-static {p1}, Lloops/TestCoroutineTwoResumeLatches;->throwOnFailure(Ljava/lang/Object;)V
    goto :first_latch

    :resume_second
    iget v2, p0, Lloops/TestCoroutineTwoResumeLatches;->secondIndex:I
    invoke-static {p1}, Lloops/TestCoroutineTwoResumeLatches;->throwOnFailure(Ljava/lang/Object;)V
    goto :second_latch

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoResumeLatches;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x0

    :first_header
    const/4 v3, 0x4
    if-ge v2, v3, :first_done
    iput v2, p0, Lloops/TestCoroutineTwoResumeLatches;->firstIndex:I
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineTwoResumeLatches;->label:I
    invoke-static {p1}, Lloops/TestCoroutineTwoResumeLatches;->suspend(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :first_latch
    return-object p2

    :first_latch
    add-int/lit8 v2, v2, 0x1
    goto :first_header

    :first_done
    const/4 v2, 0x0

    :second_header
    const/4 v3, 0x4
    if-ge v2, v3, :done
    iput v2, p0, Lloops/TestCoroutineTwoResumeLatches;->secondIndex:I
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCoroutineTwoResumeLatches;->label:I
    invoke-static {p1}, Lloops/TestCoroutineTwoResumeLatches;->suspend(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :second_latch
    return-object p2

    :second_latch
    add-int/lit8 v2, v2, 0x1
    goto :second_header

    :done
    return-object p1

    :states
    .packed-switch 0x0
        :initial
        :resume_first
        :resume_second
    .end packed-switch
.end method
