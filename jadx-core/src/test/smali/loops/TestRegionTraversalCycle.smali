.class public Lloops/TestRegionTraversalCycle;
.super Ljava/lang/Object;

.method private static test()V
    .locals 5

    const/4 v1, 0x1
    const/4 v2, 0x2
    move v3, v1

    :loop_start
    if-ge v2, v1, :exit
    if-ge v1, v2, :loop_body
    add-int/lit8 v3, v2, 0x1

    :loop_body
    const/4 v2, 0x0
    const/4 v3, 0x0

    :multi_entry
    const/4 v3, 0x0
    const/4 v2, 0x0
    move v3, v2
    const/4 v2, 0x1
    if-nez v2, :exit
    goto :loop_body

    const/4 v4, 0x0
    move v2, v4
    goto :loop_start

    :exit
    const/4 v1, 0x0
    goto :multi_entry

    return-void
.end method
