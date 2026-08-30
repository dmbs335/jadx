.class public Lcoroutines/TestCoroutinePostProcess;
.super Ljava/lang/Object;

.method private static call(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method public static run(ILjava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    .locals 4

    packed-switch p0, :switch_data

    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_0
    invoke-static {p2}, Lcoroutines/TestCoroutinePostProcess;->call(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p1, :after_1
    goto :suspended

    :state_1
    move-object v0, p2
    goto :after_1

    :after_1
    invoke-static {v0}, Lcoroutines/TestCoroutinePostProcess;->call(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p1, :post_process

    :suspended
    return-object p1

    :state_2
    move-object v0, p2

    :post_process
    const/4 v1, 0x0
    array-length v2, p3

    :loop
    if-ge v1, v2, :done
    aget-object v3, p3, v1
    if-nez v3, :next
    move-object v0, v3

    :next
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :done
    return-object v0

    :switch_data
    .packed-switch 0x0
        :state_0
        :state_1
        :state_2
    .end packed-switch
.end method
