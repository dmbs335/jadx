.class public Lloops/TestLoopEndEntryTargetUpdate;
.super Ljava/lang/Object;

.method public static nextSmallerPrime(I)I
    .locals 1

    const/4 v0, 0x2
    if-gt p0, v0, :check_three
    return v0

    :check_three
    const/4 v0, 0x3
    if-ne p0, v0, :select_start
    const/4 v0, 0x2
    return v0

    :select_start
    and-int/lit8 v0, p0, 0x1
    if-nez v0, :odd
    add-int/lit8 p0, p0, -0x1
    goto :loop_check

    :odd
    add-int/lit8 p0, p0, -0x2

    :loop_check
    const/4 v0, 0x3
    if-le p0, v0, :done
    invoke-static {p0}, Lloops/TestLoopEndEntryTargetUpdate;->isPrime(I)Z
    move-result v0
    if-nez v0, :done
    add-int/lit8 p0, p0, -0x2
    goto :loop_check

    :done
    return p0
.end method

.method private static isPrime(I)Z
    .locals 1

    const/4 v0, 0x0
    return v0
.end method
