.class public Lloops/TestCoroutineManyStateTerminalLoopReentry;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private mode:I

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    packed-switch v1, :state_switch

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget v4, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->mode:I
    if-eqz v4, :terminal_nine
    if-gez v4, :terminal_ten
    goto :call_one

    :state_one
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_one

    :state_two
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_two

    :state_three
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_three

    :state_four
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_four

    :state_five
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_five

    :state_six
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_six

    :state_seven
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_seven

    :state_eight
    const/4 v4, 0x0
    :try_start_eight
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_eight
    goto :call_one
    .catchall {:try_start_eight .. :try_end_eight} :state_eight_error

    :state_eight_error
    move-exception v4
    throw v4

    :state_nine
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    return-object p1

    :state_ten
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    return-object p1

    :call_one
    const/4 v2, 0x1
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_one
    const/4 v2, 0x2
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_two
    const/4 v2, 0x3
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_three
    const/4 v2, 0x4
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_four
    const/4 v2, 0x5
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_five
    const/4 v2, 0x6
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_six
    const/4 v2, 0x7
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3

    :after_seven
    const/16 v2, 0x8
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :call_one

    :terminal_nine
    const/16 v2, 0x9
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    return-object v3

    :terminal_ten
    const/16 v2, 0xa
    iput v2, p0, Lloops/TestCoroutineManyStateTerminalLoopReentry;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateTerminalLoopReentry;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    return-object v3

    :suspended
    return-object v0

    :state_switch
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
        :state_three
        :state_four
        :state_five
        :state_six
        :state_seven
        :state_eight
        :state_nine
        :state_ten
    .end packed-switch
.end method
