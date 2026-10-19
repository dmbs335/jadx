.class public Ltrycatch/TestSharedSwitchProtectedResultUse;
.super Ljava/lang/Object;

.method public static run(IZZ)I
    .locals 2

    packed-switch p0, :switch_data
    const/4 v0, 0x4
    return v0

    :case_zero
    goto :shared

    :case_one
    goto :shared

    :shared
    :try_start
    invoke-static {p1, p2}, Ljadx/tests/integration/trycatch/TestSharedSwitchProtectedResultUse;->protectedValue(ZZ)I
    move-result v0
    :try_end
    .catch Ljadx/tests/integration/trycatch/TestSharedSwitchProtectedResultUse$Timeout; {:try_start .. :try_end} :timeout
    .catchall {:try_start .. :try_end} :other

    if-eqz v0, :zero
    const/4 v0, 0x1
    return v0

    :zero
    const/4 v0, 0x0
    return v0

    :timeout
    move-exception v1
    const/4 v0, 0x2
    return v0

    :other
    move-exception v1
    const/4 v0, 0x3
    return v0

    :switch_data
    .packed-switch 0x0
        :case_zero
        :case_one
    .end packed-switch
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSharedSwitchProtectedResultUse;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSharedSwitchProtectedResultUse;->check(Ljava/lang/Class;)V
    return-void
.end method
