.class public Ltrycatch/TestNonThrowingSharedHandlerTail;
.super Ljava/lang/Object;

.method public static parse(Ljava/lang/String;)I
    .locals 2

    :dead_try_one_start
    const-string v0, "first"
    :dead_try_one_end
    .catch Ljava/lang/IllegalStateException; {:dead_try_one_start .. :dead_try_one_end} :dead_handler_one

    :dead_try_two_start
    const-string v0, "second"
    :dead_try_two_end
    .catch Ljava/lang/IllegalArgumentException; {:dead_try_two_start .. :dead_try_two_end} :dead_handler_two

    :live_try_start
    invoke-static {p0}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I
    move-result v0
    :live_try_end
    .catch Ljava/lang/NumberFormatException; {:live_try_start .. :live_try_end} :live_handler

    return v0

    :dead_handler_one
    move-exception v1
    goto :dead_shared_tail

    :dead_handler_two
    move-exception v1
    const/4 v0, 0x2

    :dead_shared_tail
    add-int/lit8 v0, v0, 0x1
    return v0

    :live_handler
    move-exception v1
    const/4 v0, -0x1
    return v0
.end method
