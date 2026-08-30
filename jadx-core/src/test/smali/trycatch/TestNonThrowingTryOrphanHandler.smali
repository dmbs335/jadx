.class public Ltrycatch/TestNonThrowingTryOrphanHandler;
.super Ljava/lang/Object;

.method public static parse(Ljava/lang/String;)I
    .locals 2

    :dead_try_start
    const-string v0, "unused"
    :dead_try_end
    .catch Ljava/lang/IllegalStateException; {:dead_try_start .. :dead_try_end} :dead_handler

    :live_try_start
    invoke-static {p0}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I
    move-result v0
    :live_try_end
    .catch Ljava/lang/NumberFormatException; {:live_try_start .. :live_try_end} :live_handler

    return v0

    :dead_handler
    move-exception v1
    const/4 v0, 0x1
    return v0

    :live_handler
    move-exception v1
    const/4 v0, -0x1
    return v0
.end method
