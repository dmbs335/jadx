.class public Lloops/TestLoopHeaderRegionBoundary;
.super Ljava/lang/Object;

.method public static test(I[I)I
    .registers 6

    const/4 v0, 0x0
    const/4 v1, 0x0

    :loop
    if-nez v1, :exit

    aget v2, p1, p0
    rem-int/lit8 v3, v2, 0x3
    packed-switch v3, :switch_data

    add-int/2addr v0, v2
    goto :update

    :case_zero
    if-lez v0, :continue_path
    const/4 v1, 0x1
    goto :update

    :continue_path
    add-int/lit8 p0, p0, 0x1
    goto :loop

    :case_one
    add-int/lit8 v0, v0, 0x2

    :update
    if-eqz v2, :latch_first
    if-lez v0, :latch_second
    add-int/lit8 p0, p0, 0x1
    goto :loop

    :latch_first
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :latch_second
    add-int/lit8 p0, p0, 0x2
    goto :loop

    :exit
    return v0

    :switch_data
    .packed-switch 0x0
        :case_zero
        :case_one
    .end packed-switch
.end method
