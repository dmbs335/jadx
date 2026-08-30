.class public Lloops/TestCoroutineManyStateSwitchCompletions;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private rounds:I

.method private static hasNext()Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    packed-switch v1, :state_switch

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
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
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_eight

    :call_one
    const/4 v2, 0x1
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_one

    :after_one
    const/4 v2, 0x2
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_two

    :after_two
    const/4 v2, 0x3
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_three

    :after_three
    const/4 v2, 0x4
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_four

    :after_four
    const/4 v2, 0x5
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_five

    :after_five
    const/4 v2, 0x6
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_six

    :after_six
    const/4 v2, 0x7
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_seven

    :after_seven
    const/16 v2, 0x8
    iput v2, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineManyStateSwitchCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object p1, v3
    goto :after_eight

    :after_eight
    invoke-static {}, Lloops/TestCoroutineManyStateSwitchCompletions;->hasNext()Z
    move-result v4
    if-eqz v4, :done
    iget v4, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->rounds:I
    add-int/lit8 v4, v4, -0x1
    iput v4, p0, Lloops/TestCoroutineManyStateSwitchCompletions;->rounds:I
    if-gez v4, :call_one
    :done
    return-object p1

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
    .end packed-switch
.end method
