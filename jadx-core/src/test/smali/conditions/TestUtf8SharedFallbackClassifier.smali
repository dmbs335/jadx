.class public Lconditions/TestUtf8SharedFallbackClassifier;
.super Ljava/lang/Object;

.method public static decodeStep([BII[C)I
    .locals 5

    add-int/lit8 v0, p1, 0x2
    if-gt p2, v0, :have_two_continuations

    const/16 v4, 0xfffd
    const/4 v0, 0x0
    aput-char v4, p3, v0

    add-int/lit8 v0, p1, 0x1
    if-le p2, v0, :step_one

    aget-byte v1, p0, v0
    and-int/lit16 v1, v1, 0xc0
    const/16 v2, 0x80
    if-ne v1, v2, :step_one
    goto :step_two

    :have_two_continuations
    add-int/lit8 v0, p1, 0x1
    aget-byte v1, p0, v0
    and-int/lit16 v1, v1, 0xc0
    const/16 v2, 0x80
    if-ne v1, v2, :invalid_first

    add-int/lit8 v0, p1, 0x2
    aget-byte v1, p0, v0
    and-int/lit16 v1, v1, 0xc0
    if-ne v1, v2, :invalid_second

    const/4 v3, 0x3
    goto :join

    :invalid_first
    const/16 v4, 0xfffd
    const/4 v0, 0x0
    aput-char v4, p3, v0
    goto :step_one

    :invalid_second
    const/16 v4, 0xfffd
    const/4 v0, 0x0
    aput-char v4, p3, v0

    :step_two
    const/4 v3, 0x2
    goto :join

    :step_one
    const/4 v3, 0x1

    :join
    return v3
.end method
