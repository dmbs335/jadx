.class public Lcoroutines/TestCoroutineSuspendLambda;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private emitter:Lcoroutines/TestCoroutineSuspendLambda$Emitter;
.field private job:Lcoroutines/TestCoroutineSuspendLambda$Emitter;
.field private label:I

.method private static check(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .locals 6

    iget v0, p0, Lcoroutines/TestCoroutineSuspendLambda;->label:I
    const/4 v1, 0x0
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v0, :state_0
    if-eq v0, v4, :state_1
    if-eq v0, v3, :state_2
    if-ne v0, v2, :bad_state
    invoke-static {p1}, Lcoroutines/TestCoroutineSuspendLambda;->check(Ljava/lang/Object;)V
    goto :done

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_2
    iget-object v5, p0, Lcoroutines/TestCoroutineSuspendLambda;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lcoroutines/TestCoroutineSuspendLambda;->check(Ljava/lang/Object;)V
    goto :after_2

    :state_1
    invoke-static {p1}, Lcoroutines/TestCoroutineSuspendLambda;->check(Ljava/lang/Object;)V
    goto :after_1

    :state_0
    invoke-static {p1}, Lcoroutines/TestCoroutineSuspendLambda;->check(Ljava/lang/Object;)V
    iget-object v0, p0, Lcoroutines/TestCoroutineSuspendLambda;->job:Lcoroutines/TestCoroutineSuspendLambda$Emitter;
    iput v4, p0, Lcoroutines/TestCoroutineSuspendLambda;->label:I
    invoke-interface {v0, p0}, Lcoroutines/TestCoroutineSuspendLambda$Emitter;->join(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :after_1
    goto :suspended

    :after_1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    new-instance v5, Ljava/lang/Object;
    invoke-direct {v5}, Ljava/lang/Object;-><init>()V
    iget-object v4, p0, Lcoroutines/TestCoroutineSuspendLambda;->emitter:Lcoroutines/TestCoroutineSuspendLambda$Emitter;
    iput-object v5, p0, Lcoroutines/TestCoroutineSuspendLambda;->L$0:Ljava/lang/Object;
    iput v3, p0, Lcoroutines/TestCoroutineSuspendLambda;->label:I
    invoke-interface {v4, v0, p0}, Lcoroutines/TestCoroutineSuspendLambda$Emitter;->emit(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :after_2
    goto :suspended

    :after_2
    iget-object v0, p0, Lcoroutines/TestCoroutineSuspendLambda;->emitter:Lcoroutines/TestCoroutineSuspendLambda$Emitter;
    iput-object v1, p0, Lcoroutines/TestCoroutineSuspendLambda;->L$0:Ljava/lang/Object;
    iput v2, p0, Lcoroutines/TestCoroutineSuspendLambda;->label:I
    invoke-interface {v0, v5, p0}, Lcoroutines/TestCoroutineSuspendLambda$Emitter;->emit(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :done

    :suspended
    return-object p2

    :done
    move-object v0, p1
    const/4 v1, 0x0
    array-length v2, p3

    :loop
    if-ge v1, v2, :return
    aget-object v3, p3, v1
    if-nez v3, :next
    move-object v0, v3

    :next
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :return
    return-object v0
.end method
