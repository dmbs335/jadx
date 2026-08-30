.class public Lloops/TestTryProtectedIteratorMappingCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private iterator:Ljava/util/Iterator;
.field private label:I
.field private result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 1
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static iterator()Ljava/util/Iterator;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static mapResult(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static suspendMap(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 7

    iget-object p1, p0, Lloops/TestTryProtectedIteratorMappingCompletion;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedIteratorMappingCompletion;->label:I
    if-eqz v1, :state_zero
    const/4 v2, 0x1
    if-eq v1, v2, :resume_mapping
    const/4 v2, 0x2
    if-ne v1, v2, :bad_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object v2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v2

    :resume_mapping
    iget-object v2, p0, Lloops/TestTryProtectedIteratorMappingCompletion;->iterator:Ljava/util/Iterator;
    :try_start_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catch Ljava/lang/Exception; {:try_start_resume .. :try_end_resume} :catch_mapping
    goto :map_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestTryProtectedIteratorMappingCompletion;->iterator()Ljava/util/Iterator;
    move-result-object v2

    :loop
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z
    move-result v3
    if-eqz v3, :done
    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v4
    :try_start_call
    iput-object v2, p0, Lloops/TestTryProtectedIteratorMappingCompletion;->iterator:Ljava/util/Iterator;
    const/4 v5, 0x1
    iput v5, p0, Lloops/TestTryProtectedIteratorMappingCompletion;->label:I

    invoke-static {v4, p0}, Lloops/TestTryProtectedIteratorMappingCompletion;->suspendMap(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :map_result
    invoke-static {p1}, Lloops/TestTryProtectedIteratorMappingCompletion;->mapResult(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    :try_end_call
    .catch Ljava/lang/Exception; {:try_start_call .. :try_end_call} :catch_mapping

    goto :mapped_decision

    :catch_mapping
    move-exception v6
    sget-object v6, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    goto :mapped_decision

    :mapped_decision
    if-eqz v6, :loop
    return-object v6

    :done
    sget-object v6, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v6

    :suspended
    return-object v0
.end method
