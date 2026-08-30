.class public Ltypes/TestBooleanLiteralNumericPhi;
.super Ljava/lang/Object;

.field private valid:Z

.method public test(Ljava/lang/Integer;Z)Z
    .registers 6

    if-eqz p2, :bool_false
    const/4 v0, 0x0
    goto :bool_merge

    :bool_false
    const/4 v0, 0x0

    :bool_merge
    iput-boolean v0, p0, Ltypes/TestBooleanLiteralNumericPhi;->valid:Z
    if-eqz p1, :null_value
    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I
    move-result v1
    goto :numeric_merge

    :null_value
    move v1, v0

    :numeric_merge
    if-lez v1, :not_positive
    const/4 v2, 0x1
    return v2

    :not_positive
    const/4 v2, 0x0
    return v2
.end method
