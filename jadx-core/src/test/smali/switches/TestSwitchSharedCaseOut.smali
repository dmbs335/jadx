.class public Lswitches/TestSwitchSharedCaseOut;
.super Ljava/lang/Object;

.method public static test(IZ)I
    .registers 2

    sparse-switch p0, :switch_data
    goto :shared_out

    :case_1
    if-eqz p1, :shared_out
    const/4 p0, 0x1
    return p0

    :case_2
    if-eqz p1, :shared_out
    const/4 p0, 0x2
    return p0

    :case_3
    if-eqz p1, :shared_out
    const/4 p0, 0x3
    return p0

    :shared_out
    const/4 p0, 0x0
    return p0

    :switch_data
    .sparse-switch
        0x1 -> :case_1
        0x2 -> :case_2
        0x3 -> :case_3
    .end sparse-switch
.end method
