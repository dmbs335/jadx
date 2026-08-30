.class public Lcoroutines/TestCoroutineMoveBridge;
.super Ljava/lang/Object;

.method public static run(I[Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 3

    packed-switch p0, :switch_data
    goto :done

    :state_0
    const/4 v0, 0x0
    move-object v1, p2
    goto :loop

    :state_1
    const/4 v0, 0x1
    move-object v1, p2
    goto :next

    :loop
    array-length v2, p1
    if-ge v0, v2, :done
    aget-object v2, p1, v0
    instance-of p0, v2, Ljava/lang/String;
    if-eqz p0, :next
    move-object v1, v2

    :next
    move-object p2, v1
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :done
    return-object p2

    :switch_data
    .packed-switch 0x0
        :state_0
        :state_1
    .end packed-switch
.end method
