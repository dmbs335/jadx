.class public Lconditions/TestRedundantConstBranchMove;
.super Ljava/lang/Object;

.method private static invalid(B)Z
    .registers 2

    const/16 v0, -0x41
    if-le p0, v0, :valid
    const/4 v0, 0x1
    return v0

    :valid
    const/4 v0, 0x0
    return v0
.end method

.method public static test(BBB[CI)V
    .registers 8

    invoke-static {p1}, Lconditions/TestRedundantConstBranchMove;->invalid(B)Z
    move-result v0
    if-nez v0, :error

    const/16 v0, -0x60
    const/16 v1, -0x20
    if-ne p0, v1, :check_second
    if-lt p1, v0, :error
    move p0, v1

    :check_second
    const/16 v1, -0x13
    if-ne p0, v1, :check_tail
    if-ge p1, v0, :error
    move p0, v1

    :check_tail
    invoke-static {p2}, Lconditions/TestRedundantConstBranchMove;->invalid(B)Z
    move-result v0
    if-nez v0, :error

    and-int/lit8 v0, p0, 0xf
    shl-int/lit8 v0, v0, 0xc
    and-int/lit8 v1, p1, 0x3f
    shl-int/lit8 v1, v1, 0x6
    or-int/2addr v0, v1
    and-int/lit8 v1, p2, 0x3f
    or-int/2addr v0, v1
    int-to-char v0, v0
    aput-char v0, p3, p4
    return-void

    :error
    new-instance v0, Ljava/lang/IllegalArgumentException;
    invoke-direct {v0}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw v0
.end method
