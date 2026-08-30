.class public Lswitches/TestSwitchTerminalCaseSharedNormalTail;
.super Ljava/lang/Object;

.method private static consume(I)V
    .registers 1
    return-void
.end method

.method public static test(IIII)I
    .registers 6

    const/4 v0, 0x0

    :loop
    if-lez p2, :exit
    packed-switch p0, :outer_switch
    goto :outer_out

    :outer_case
    sparse-switch p1, :inner_switch
    goto :inner_default

    :inner_case_1
    const/4 v0, 0x1
    goto :group_a

    :inner_case_2
    const/4 v0, 0x2
    goto :group_a

    :inner_case_3
    const/4 v0, 0x3
    goto :group_b

    :inner_case_4
    const/4 v0, 0x4
    goto :group_b

    :group_a
    if-nez p3, :inner_default
    goto :inner_join

    :group_b
    if-nez p3, :inner_default
    goto :inner_join

    :inner_default
    new-instance v1, Ljava/lang/IllegalArgumentException;
    invoke-direct {v1}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw v1

    :inner_join
    invoke-static {v0}, Lswitches/TestSwitchTerminalCaseSharedNormalTail;->consume(I)V

    :outer_out
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :exit
    return v0

    :outer_switch
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch
    .sparse-switch
        0x1 -> :inner_case_1
        0x2 -> :inner_case_2
        0x3 -> :inner_case_3
        0x4 -> :inner_case_4
    .end sparse-switch
.end method
