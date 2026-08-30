.class public final Lloops/TestCoroutineRegexYieldResumeLoop;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;
.source "Regex.kt"

.field I$0:I
.field I$1:I
.field private synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field input:Ljava/lang/CharSequence;
.field label:I
.field limit:I
.field pattern:Ljava/util/regex/Pattern;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    iget-object v0, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->L$0:Ljava/lang/Object;
    check-cast v0, Lkotlin/sequences/SequenceScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->label:I
    const/4 v3, 0x3
    const/4 v4, 0x2
    const/4 v5, 0x1
    if-eqz v2, :initial
    if-eq v2, v5, :resume_done
    if-eq v2, v4, :resume_loop
    if-ne v2, v3, :invalid_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :return_unit

    :invalid_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to resume before invoke"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :resume_loop
    iget v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->I$1:I
    iget-object v6, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->L$1:Ljava/lang/Object;
    check-cast v6, Ljava/util/regex/Matcher;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :advance

    :resume_done
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :return_unit

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->pattern:Ljava/util/regex/Pattern;
    iget-object v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->input:Ljava/lang/CharSequence;
    invoke-virtual {p1, v2}, Ljava/util/regex/Pattern;->matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;
    move-result-object p1
    iget v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->limit:I
    if-eq v2, v5, :no_match
    invoke-virtual {p1}, Ljava/util/regex/Matcher;->find()Z
    move-result v2
    if-eqz v2, :no_match
    const/4 v2, 0x0
    move-object v6, p1
    move p1, v2

    :loop_body
    iget-object v7, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->input:Ljava/lang/CharSequence;
    invoke-virtual {v6}, Ljava/util/regex/Matcher;->start()I
    move-result v8
    invoke-interface {v7, v2, v8}, Ljava/lang/CharSequence;->subSequence(II)Ljava/lang/CharSequence;
    move-result-object v7
    invoke-virtual {v7}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v7
    iput-object v0, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->I$0:I
    iput p1, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->I$1:I
    iput v4, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->label:I
    invoke-virtual {v0, v7, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v1, :suspended
    move v2, p1

    :advance
    invoke-virtual {v6}, Ljava/util/regex/Matcher;->end()I
    move-result p1
    add-int/2addr v2, v5
    iget v7, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->limit:I
    sub-int/2addr v7, v5
    if-eq v2, v7, :final_segment
    invoke-virtual {v6}, Ljava/util/regex/Matcher;->find()Z
    move-result v7
    if-eqz v7, :final_segment
    move v9, v2
    move v2, p1
    move p1, v9
    goto :loop_body

    :final_segment
    iget-object v4, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->input:Ljava/lang/CharSequence;
    invoke-interface {v4}, Ljava/lang/CharSequence;->length()I
    move-result v5
    invoke-interface {v4, p1, v5}, Ljava/lang/CharSequence;->subSequence(II)Ljava/lang/CharSequence;
    move-result-object v4
    invoke-virtual {v4}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v4
    iput p1, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->I$0:I
    iput v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->I$1:I
    iput v3, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->label:I
    invoke-virtual {v0, v4, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended
    goto :return_unit

    :no_match
    iget-object v2, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->input:Ljava/lang/CharSequence;
    invoke-virtual {v2}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v2
    iput v5, p0, Lloops/TestCoroutineRegexYieldResumeLoop;->label:I
    invoke-virtual {v0, v2, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended

    :return_unit
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :suspended
    return-object v1
.end method
