.class public Lconditions/TestSharedOutReadOnly;
.super Ljava/lang/Object;

.method public static align(IFF)F
    .locals 1

    const/4 v0, 0x2
    if-eq p0, v0, :half
    const/4 v0, 0x5
    if-eq p0, v0, :half
    const/16 v0, 0x8
    if-ne p0, v0, :out

    sub-float/2addr p1, p2
    goto :apply

    :half
    sub-float/2addr p1, p2
    const/high16 v0, 0x40000000    # 2.0f
    div-float/2addr p1, v0

    :apply
    neg-float p1, p1

    :out
    return p1
.end method
