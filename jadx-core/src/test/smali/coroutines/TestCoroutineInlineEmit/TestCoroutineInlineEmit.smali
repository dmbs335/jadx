.class public Lcoroutines/TestCoroutineInlineEmit;
.super Ljava/lang/Object;

.field private collector:Ljava/lang/Object;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lcoroutines/TestCoroutineInlineEmit;->collector:Ljava/lang/Object;
    return-void
.end method

.method private static call(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method public emit(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .locals 7

    instance-of v0, p2, Lcoroutines/TestCoroutineInlineEmit$Continuation;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;
    iget v1, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;
    invoke-direct {v0, p2}, Lcoroutines/TestCoroutineInlineEmit$Continuation;-><init>(Ljava/lang/Object;)V

    :dispatch
    iget-object p2, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->result:Ljava/lang/Object;
    iget v1, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :state_0
    if-eq v1, v3, :state_1
    if-ne v1, v2, :bad_state
    goto :done

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_1
    iget-object p1, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->L$0:Ljava/lang/Object;
    goto :after_1

    :state_0
    iget-object p2, p0, Lcoroutines/TestCoroutineInlineEmit;->collector:Ljava/lang/Object;
    iput-object p2, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->L$0:Ljava/lang/Object;
    iput v3, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->label:I
    invoke-static {p1, v0}, Lcoroutines/TestCoroutineInlineEmit;->call(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, p3, :restore
    goto :suspended

    :restore
    move-object v6, p2
    move-object p2, p1
    move-object p1, v6

    :after_1
    const/4 v1, 0x0
    iput-object v1, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->L$0:Ljava/lang/Object;
    iput v2, v0, Lcoroutines/TestCoroutineInlineEmit$Continuation;->label:I
    invoke-static {p2, v0}, Lcoroutines/TestCoroutineInlineEmit;->call(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, p3, :done

    :suspended
    return-object p3

    :done
    move-object v0, p2
    const/4 v1, 0x0
    array-length v2, p4

    :loop
    if-ge v1, v2, :return
    aget-object v3, p4, v1
    if-nez v3, :next
    move-object v0, v3

    :next
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :return
    return-object v0
.end method
