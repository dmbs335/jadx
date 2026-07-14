.class public Lswitches/TestSwitchSharedIfTail;
.super Ljava/lang/Object;

.method public static test(III)I
    .registers 5

    const/4 v0, 0x0
    packed-switch p0, :switch_data
    goto :exit

    :case_0
    and-int/lit8 v1, p1, 0x1
    if-eqz v1, :exit
    goto :shared_tail

    :case_1
    and-int/lit8 v1, p2, 0x1
    if-eqz v1, :exit

    :shared_tail
    invoke-static {p0}, Lswitches/TestSwitchSharedIfTail;->calc(I)I
    move-result v1
    add-int/2addr v0, v1

    :exit
    return v0

    :switch_data
    .packed-switch 0x0
        :case_0
        :case_1
    .end packed-switch
.end method

.method private static calc(I)I
    .registers 1
    add-int/lit8 p0, p0, 0x1
    return p0
.end method
