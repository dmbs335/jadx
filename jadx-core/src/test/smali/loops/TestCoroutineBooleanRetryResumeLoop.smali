.class public final Lloops/TestCoroutineBooleanRetryResumeLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "LottieAnimatable.kt"

.field active:Z
.field iteration:I
.field iterations:I
.field label:I
.field mode:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public static native doFrame(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 7

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->label:I
    const/4 v2, 0x1
    if-eqz v1, :initial
    if-ne v1, v2, :invalid_state

    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :retry
    iget v3, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->mode:I
    if-ne v3, v2, :default_iterations
    iget-boolean v3, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->active:Z
    if-eqz v3, :current_iteration
    iget v3, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->iterations:I
    goto :call

    :current_iteration
    iget v3, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->iteration:I
    goto :call

    :default_iterations
    iget v3, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->iterations:I

    :call
    iput v2, p0, Lloops/TestCoroutineBooleanRetryResumeLoop;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineBooleanRetryResumeLoop;->doFrame(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :result
    return-object v0

    :result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v3
    if-nez v3, :retry
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :invalid_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to resume before invoke"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1
.end method
