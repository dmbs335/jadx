.class public Ltypes/TestSharedBooleanPhiBranch;
.super Ljava/lang/Object;

.method public static test(ZZZ)[Z
    .registers 6

    const/4 v0, 0x1
    if-eqz p1, :shared_default
    if-nez p2, :merge

    :shared_default
    move v0, p0

    :merge
    const/4 v1, 0x2
    new-array v1, v1, [Z
    const/4 v2, 0x0
    aput-boolean p0, v1, v2
    const/4 v2, 0x1
    aput-boolean v0, v1, v2
    return-object v1
.end method
