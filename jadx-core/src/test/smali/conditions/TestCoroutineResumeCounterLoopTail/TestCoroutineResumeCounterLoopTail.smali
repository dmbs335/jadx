.class public final Lconditions/TestCoroutineResumeCounterLoopTail;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.implements Lkotlin/jvm/functions/Function2;

.field L$0:Ljava/lang/Object;
.field label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->label:I
    if-eqz v1, :initial

    const/4 v2, 0x1
    if-eq v1, v2, :receive_resume
    const/4 v2, 0x2
    if-ne v1, v2, :bad_state

    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :counter_tail

    :receive_resume
    iget-object v2, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :consume_call

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :loop
    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    iput-object v2, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->L$0:Ljava/lang/Object;
    const/4 v1, 0x1
    iput v1, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->label:I
    invoke-static {p0}, Lconditions/TestCoroutineResumeCounterLoopTail;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :consume_call
    goto :suspended

    :consume_call
    const/4 v1, 0x0
    iput-object v1, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->L$0:Ljava/lang/Object;
    const/4 v1, 0x2
    iput v1, p0, Lconditions/TestCoroutineResumeCounterLoopTail;->label:I
    invoke-static {v2, p1, p0}, Lconditions/TestCoroutineResumeCounterLoopTail;->consume(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :counter_tail

    :suspended
    return-object v0

    :counter_tail
    invoke-static {}, Lconditions/TestCoroutineResumeCounterLoopTail;->decrementAndGet()I
    move-result v1
    if-nez v1, :loop

    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method

.method private static receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static consume(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static decrementAndGet()I
    .registers 1

    const/4 v0, 0x0
    return v0
.end method
