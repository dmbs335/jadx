.class public Lconditions/TestInvalidBooleanRelationalCompare;
.super Ljava/lang/Object;

.method public static check(Z)I
    .locals 1

    const/4 v0, 0x1
    if-lt p0, v0, :less
    const/4 v0, 0x2
    return v0

    :less
    const/4 v0, 0x3
    return v0
.end method

.method public static checkNonBooleanLiteral(Z)I
    .locals 1

    const/4 v0, 0x2
    if-lt p0, v0, :less
    const/4 v0, 0x4
    return v0

    :less
    const/4 v0, 0x5
    return v0
.end method

.method public static checkTwoBooleans(ZZ)I
    .locals 1

    if-lt p0, p1, :less
    const/4 v0, 0x6
    return v0

    :less
    const/4 v0, 0x7
    return v0
.end method

.method public static checkEqualNonBooleanLiteral(Z)I
    .locals 1

    const/4 v0, 0x2
    if-eq p0, v0, :equal
    const/16 v0, 0x8
    return v0

    :equal
    const/16 v0, 0x9
    return v0
.end method

.method public static checkEqualBooleans(ZZ)I
    .locals 1

    if-eq p0, p1, :equal
    const/16 v0, 0xa
    return v0

    :equal
    const/16 v0, 0xb
    return v0
.end method

.method public static checkLiteralLeft(Z)I
    .locals 1

    const/4 v0, 0x1
    if-eq v0, p0, :equal
    const/4 v0, 0x4
    return v0

    :equal
    const/4 v0, 0x5
    return v0
.end method
