.class public Lconditions/TestUtf8SharedFallbackMoveBridge;
.super Ljava/lang/Object;

.method public static commonToUtf8String([BII[C)I
    .locals 7

    const/4 v1, 0x0

    :loop
    if-ge p1, p2, :done

    aget-byte v0, p0, p1
    shr-int/lit8 v3, v0, 0x5
    const/4 v4, -0x2
    if-ne v3, v4, :try_three

    add-int/lit8 v3, p1, 0x1
    if-gt p2, v3, :full_two

    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1

    :step_one
    move v1, v6
    const/4 v2, 0x1
    goto :advance

    :full_two
    aget-byte v4, p0, v3
    and-int/lit16 v4, v4, 0xc0
    const/16 v5, 0x80
    if-ne v4, v5, :invalid_two

    add-int/lit8 v6, v1, 0x1
    const/16 v5, 0x62
    aput-char v5, p3, v1

    :step_two
    move v1, v6
    const/4 v2, 0x2
    goto :advance

    :invalid_two
    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1
    goto :step_one

    :advance
    add-int/2addr p1, v2
    goto :loop

    :try_three
    shr-int/lit8 v3, v0, 0x4
    if-ne v3, v4, :try_four

    add-int/lit8 v3, p1, 0x2
    if-gt p2, v3, :full_sequence

    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1

    add-int/lit8 v1, p1, 0x1
    if-le p2, v1, :step_one

    aget-byte v1, p0, v1
    and-int/lit16 v1, v1, 0xc0
    const/16 v5, 0x80
    if-ne v1, v5, :step_one
    goto :step_two

    :full_sequence
    add-int/lit8 v3, p1, 0x1
    aget-byte v4, p0, v3
    and-int/lit16 v4, v4, 0xc0
    const/16 v5, 0x80
    if-ne v4, v5, :invalid_first

    add-int/lit8 v3, p1, 0x2
    aget-byte v4, p0, v3
    and-int/lit16 v4, v4, 0xc0
    if-ne v4, v5, :invalid_second

    add-int/lit8 v6, v1, 0x1
    const/16 v5, 0x61
    aput-char v5, p3, v1

    :step_three
    move v1, v6
    const/4 v2, 0x3
    goto :advance

    :invalid_first
    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1
    goto :step_one

    :invalid_second
    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1
    goto :step_two

    :try_four
    shr-int/lit8 v3, v0, 0x3
    if-ne v3, v4, :other

    add-int/lit8 v3, p1, 0x3
    if-gt p2, v3, :full_four

    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1

    add-int/lit8 v1, p1, 0x1
    if-le p2, v1, :step_one
    aget-byte v1, p0, v1
    and-int/lit16 v1, v1, 0xc0
    const/16 v4, 0x80
    if-ne v1, v4, :step_one

    add-int/lit8 v1, p1, 0x2
    if-le p2, v1, :step_two
    aget-byte v1, p0, v1
    and-int/lit16 v1, v1, 0xc0
    if-ne v1, v4, :step_two
    goto :step_three

    :full_four
    add-int/lit8 v6, v1, 0x1
    const/16 v5, 0x63
    aput-char v5, p3, v1
    move v1, v6
    const/4 v2, 0x4
    goto :advance

    :other
    const/16 v5, 0xfffd
    add-int/lit8 v6, v1, 0x1
    aput-char v5, p3, v1
    add-int/lit8 p1, p1, 0x1
    move v1, v6
    goto :loop

    :done
    return v1
.end method
