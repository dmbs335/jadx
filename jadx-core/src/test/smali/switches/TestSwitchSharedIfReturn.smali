.class public Lswitches/TestSwitchSharedIfReturn;
.super Ljava/lang/Object;

.method public static test(ILjava/lang/String;)I
    .registers 5

    packed-switch p0, :switch_data
    goto :fallback

    :case_0
    const-string v1, "Congestion"
    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :fallback
    goto :shared_return

    :case_1
    const-string v1, "SevereCongestion"
    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :fallback

    :shared_return
    if-eqz p0, :small
    const/16 v0, 0x64
    return v0

    :small
    const/16 v0, 0xa
    return v0

    :fallback
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_0
        :case_1
    .end packed-switch
.end method
