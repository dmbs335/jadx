.class public Ltypes/TestBooleanLiteralLoopIndexPhi;
.super Ljava/lang/Object;

.method private static acceptBoolean(Z)V
    .registers 1
    return-void
.end method

.method public static test(ZZI[I)I
    .registers 9

    const/4 v0, 0x0
    invoke-static {v0}, Ltypes/TestBooleanLiteralLoopIndexPhi;->acceptBoolean(Z)V

    if-eqz p0, :forward
    add-int/lit8 v1, p2, -0x1
    const/4 v2, -0x1
    goto :selected

    :forward
    move v1, v0
    const/4 v2, 0x1

    :selected
    const/4 v3, 0x0
    if-eqz p1, :second_loop
    move v4, v1

    :first_next
    if-eq v4, p2, :done
    aget v5, p3, v4
    add-int/2addr v3, v5
    add-int/2addr v4, v2
    goto :first_next

    :second_loop
    move v4, v1

    :second_next
    if-eq v4, p2, :done
    aget v5, p3, v4
    sub-int/2addr v3, v5
    add-int/2addr v4, v2
    goto :second_next

    :done
    return v3
.end method
