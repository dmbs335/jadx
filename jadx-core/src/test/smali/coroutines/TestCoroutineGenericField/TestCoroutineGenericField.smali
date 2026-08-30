.class public Lcoroutines/TestCoroutineGenericField;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<T:",
        "Ljava/lang/Object;",
        ">",
        "Ljava/lang/Object;"
    }
.end annotation

.field private first:Lcoroutines/TestCoroutineGenericField$StateFlow;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Lcoroutines/TestCoroutineGenericField$StateFlow<",
            "TT;",
            ">;"
        }
    .end annotation
.end field

.field private second:Lcoroutines/TestCoroutineGenericField$StateFlow;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Lcoroutines/TestCoroutineGenericField$StateFlow<",
            "TT;",
            ">;"
        }
    .end annotation
.end field

.method private static marker()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method

.method private static check(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static clear(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public emit(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TT;",
            "Ljava/lang/Object;",
            ")",
            "Ljava/lang/Object;"
        }
    .end annotation

    instance-of v0, p2, Lcoroutines/TestCoroutineGenericField$Continuation;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lcoroutines/TestCoroutineGenericField$Continuation;
    iget v1, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lcoroutines/TestCoroutineGenericField$Continuation;
    invoke-direct {v0, p2}, Lcoroutines/TestCoroutineGenericField$Continuation;-><init>(Ljava/lang/Object;)V

    :dispatch
    iget-object p2, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->result:Ljava/lang/Object;
    invoke-static {}, Lcoroutines/TestCoroutineGenericField;->marker()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :state_0
    if-eq v2, v4, :state_1
    if-ne v2, v3, :bad_state
    invoke-static {p2}, Lcoroutines/TestCoroutineGenericField;->check(Ljava/lang/Object;)V
    goto :done

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_1
    iget-object p1, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->L$0:Ljava/lang/Object;
    invoke-static {p2}, Lcoroutines/TestCoroutineGenericField;->check(Ljava/lang/Object;)V
    goto :emit_second

    :state_0
    invoke-static {p2}, Lcoroutines/TestCoroutineGenericField;->check(Ljava/lang/Object;)V
    instance-of p2, p1, Ljava/lang/String;
    if-nez p2, :emit_first
    instance-of p2, p1, Ljava/lang/Integer;
    if-eqz p2, :emit_second

    :emit_first
    iget-object p2, p0, Lcoroutines/TestCoroutineGenericField;->first:Lcoroutines/TestCoroutineGenericField$StateFlow;
    if-eqz p2, :emit_second
    iput-object p1, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->L$0:Ljava/lang/Object;
    iput v4, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->label:I
    invoke-interface {p2, p1, v0}, Lcoroutines/TestCoroutineGenericField$SharedFlow;->emit(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p2
    if-ne p2, v1, :emit_second
    goto :suspended

    :emit_second
    iget-object p2, p0, Lcoroutines/TestCoroutineGenericField;->second:Lcoroutines/TestCoroutineGenericField$StateFlow;
    invoke-static {p1}, Lcoroutines/TestCoroutineGenericField;->clear(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    iput-object v2, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->L$0:Ljava/lang/Object;
    iput v3, v0, Lcoroutines/TestCoroutineGenericField$Continuation;->label:I
    invoke-interface {p2, p1, v0}, Lcoroutines/TestCoroutineGenericField$SharedFlow;->emit(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :done

    :suspended
    return-object v1

    :done
    const/4 p1, 0x0
    return-object p1
.end method
