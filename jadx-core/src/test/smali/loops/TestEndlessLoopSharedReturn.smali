.class public Lloops/TestEndlessLoopSharedReturn;
.super Ljava/lang/Object;

.method public static read([BIIII)I
    .locals 2

    packed-switch p4, :switch_data
    return p1

    :case_loop
    const/4 v0, 0x0

    :loop
    add-int/lit8 v0, v0, 0x1
    if-ge p1, p2, :exit

    aget-byte v1, p0, p1
    add-int/lit8 p1, p1, 0x1
    if-ne v1, p3, :exit

    add-int/lit8 p1, p1, 0x1
    goto :loop

    :exit
    return p1

    :switch_data
    .packed-switch 0x1
        :case_loop
    .end packed-switch
.end method
