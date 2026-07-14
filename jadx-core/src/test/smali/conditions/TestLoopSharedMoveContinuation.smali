.class public Lconditions/TestLoopSharedMoveContinuation;
.super Ljava/lang/Object;

.method public static test([FIZ)I
    .registers 10

    const/4 v0, 0x0
    const/4 v1, 0x0
    const/4 v6, 0x0
    array-length v2, p0

    :loop
    if-ge v0, v2, :end
    aget v3, p0, v0

    if-nez p1, :direction_1
    cmpg-float v4, v3, v6
    if-lez v4, :apply_move
    if-eqz p2, :skip_move
    cmpl-float v4, v3, v6
    if-nez v4, :apply_move
    goto :skip_move

    :direction_1
    const/4 v5, 0x1
    if-ne p1, v5, :direction_2
    cmpl-float v4, v3, v6
    if-gez v4, :apply_move
    if-eqz p2, :skip_move
    cmpg-float v4, v3, v6
    if-nez v4, :apply_move
    goto :skip_move

    :direction_2
    const/4 v5, 0x2
    if-ne p1, v5, :apply_move
    cmpg-float v4, v3, v6
    if-gez v4, :apply_move
    if-eqz p2, :skip_move
    cmpl-float v4, v3, v6
    if-nez v4, :apply_move

    :skip_move
    move v5, p1
    goto :next

    :apply_move
    move v5, p1
    goto :apply

    :apply
    add-int/lit8 v1, v1, 0x1

    :next
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :end
    return v1
.end method
