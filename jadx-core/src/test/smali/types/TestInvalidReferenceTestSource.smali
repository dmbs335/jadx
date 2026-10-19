.class public Ltypes/TestInvalidReferenceTestSource;
.super Ljava/lang/Object;

.method public static intInstanceOf(I)Z
    .locals 1
    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static intCheckCast(I)Ljava/lang/String;
    .locals 0
    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static intArrayLength(I)I
    .locals 1
    array-length v0, p0
    return v0
.end method

.method public static intThrow(I)V
    .locals 0
    throw p0
.end method

.method public static intMonitor(I)V
    .locals 0
    monitor-enter p0
    monitor-exit p0
    return-void
.end method

.method public static validInstanceOf(Ljava/lang/Object;)Z
    .locals 1
    instance-of v0, p0, Ljava/lang/String;
    return v0
.end method

.method public static validCheckCast(Ljava/lang/Object;)Ljava/lang/String;
    .locals 0
    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static validArrayLength([I)I
    .locals 1
    array-length v0, p0
    return v0
.end method

.method public static validThrow(Ljava/lang/Throwable;)V
    .locals 0
    throw p0
.end method
