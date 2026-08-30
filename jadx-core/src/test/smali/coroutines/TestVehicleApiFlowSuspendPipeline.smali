.class public Lcoroutines/ExecuteBodyEmitPipeline;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static execute(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static bodyNullable(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static emit(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method public invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->label:I

    if-eqz v1, :state_0
    const/4 v2, 0x1
    if-eq v1, v2, :state_1
    const/4 v2, 0x2
    if-eq v1, v2, :state_2
    const/4 v2, 0x3
    if-ne v1, v2, :invalid_state

    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :complete

    :invalid_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "invalid state"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :state_2
    iget-object v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_body

    :state_1
    iget-object v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_execute

    :state_0
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    iput-object v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->L$0:Ljava/lang/Object;
    const/4 v1, 0x1
    iput v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->label:I
    invoke-static {p0}, Lcoroutines/ExecuteBodyEmitPipeline;->execute(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_execute
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    iput-object v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->L$0:Ljava/lang/Object;
    const/4 v1, 0x2
    iput v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->label:I
    invoke-static {p0}, Lcoroutines/ExecuteBodyEmitPipeline;->bodyNullable(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_body
    const/4 v1, 0x0
    iput-object v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->L$0:Ljava/lang/Object;
    const/4 v1, 0x3
    iput v1, p0, Lcoroutines/ExecuteBodyEmitPipeline;->label:I
    invoke-static {p0}, Lcoroutines/ExecuteBodyEmitPipeline;->emit(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :complete
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    :suspended
    return-object v0
.end method
