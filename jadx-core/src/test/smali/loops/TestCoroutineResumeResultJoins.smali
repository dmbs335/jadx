.class public Lloops/TestCoroutineResumeResultJoins;
.super Ljava/lang/Object;

.field private index:I
.field private label:I

.method private static consume(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static finish(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static suspendA(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static suspendB(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 7

    iget v0, p0, Lloops/TestCoroutineResumeResultJoins;->label:I
    const/4 v1, 0x2
    const/4 v2, 0x3
    const/4 v3, 0x4
    if-eqz v0, :initial
    if-eq v0, v1, :resume_a
    if-eq v0, v2, :resume_b
    if-eq v0, v3, :resume_done
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_a
    iget v4, p0, Lloops/TestCoroutineResumeResultJoins;->index:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->throwOnFailure(Ljava/lang/Object;)V
    move-object v6, p1
    goto :result_a

    :resume_b
    iget v4, p0, Lloops/TestCoroutineResumeResultJoins;->index:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->throwOnFailure(Ljava/lang/Object;)V
    move-object v6, p1
    goto :result_b

    :resume_done
    iget v4, p0, Lloops/TestCoroutineResumeResultJoins;->index:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->throwOnFailure(Ljava/lang/Object;)V
    goto :done

    :initial
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v4, 0x0

    :header
    const/16 v5, 0xa
    if-ge v4, v5, :emit
    and-int/lit8 v5, v4, 0x3
    packed-switch v5, :switch_data
    goto :call_a

    :call_a
    iput v4, p0, Lloops/TestCoroutineResumeResultJoins;->index:I
    iput v1, p0, Lloops/TestCoroutineResumeResultJoins;->label:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->suspendA(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    goto :check_a

    :check_a
    if-ne v6, p2, :result_a
    goto :suspended

    :result_a
    check-cast v6, Ljava/lang/String;
    goto :post_result

    :call_b
    iput v4, p0, Lloops/TestCoroutineResumeResultJoins;->index:I
    iput v2, p0, Lloops/TestCoroutineResumeResultJoins;->label:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->suspendB(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    goto :check_b

    :check_b
    if-ne v6, p2, :result_b
    goto :suspended

    :result_b
    check-cast v6, Ljava/lang/String;

    :post_result
    if-eqz v6, :next
    invoke-static {v6}, Lloops/TestCoroutineResumeResultJoins;->consume(Ljava/lang/Object;)V

    :next
    add-int/lit8 v4, v4, 0x1
    goto :header

    :emit
    iput v3, p0, Lloops/TestCoroutineResumeResultJoins;->label:I
    invoke-static {p1}, Lloops/TestCoroutineResumeResultJoins;->finish(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    goto :check_done

    :check_done
    if-ne v6, p2, :done
    goto :suspended

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    goto :return_done

    :return_done
    return-object v0

    :suspended
    return-object p2

    :switch_data
    .packed-switch 0x0
        :call_a
        :call_b
        :call_a
        :call_b
    .end packed-switch
.end method
