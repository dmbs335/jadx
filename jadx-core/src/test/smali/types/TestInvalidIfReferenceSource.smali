.class public Ltypes/TestInvalidIfReferenceSource;
.super Ljava/lang/Object;

.method public static objectLtObject(Ljava/lang/Object;Ljava/lang/Object;)I
    .locals 1
    if-lt p0, p1, :less
    const/4 v0, 0x0
    return v0
    :less
    const/4 v0, 0x1
    return v0
.end method

.method public static objectGeZero(Ljava/lang/Object;)I
    .locals 1
    if-gez p0, :ge
    const/4 v0, 0x0
    return v0
    :ge
    const/4 v0, 0x1
    return v0
.end method

.method public static validObjectEq(Ljava/lang/Object;Ljava/lang/Object;)I
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method
