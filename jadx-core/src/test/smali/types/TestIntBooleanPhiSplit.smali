.class public Ltypes/TestIntBooleanPhiSplit;
.super Ljava/lang/Object;

.field private match:Z

.method public test(II)V
    .registers 5

    if-ne p1, p2, :not_equal
    const/4 v0, 0x1
    goto :merge

    :not_equal
    const/4 v0, 0x0

    :merge
    iput-boolean v0, p0, Ltypes/TestIntBooleanPhiSplit;->match:Z
    if-nez v0, :non_zero
    invoke-direct {p0, p1}, Ltypes/TestIntBooleanPhiSplit;->use(I)V

    :non_zero
    sub-int v1, p1, v0
    invoke-direct {p0, v1}, Ltypes/TestIntBooleanPhiSplit;->use(I)V
    return-void
.end method

.method private use(I)V
    .registers 2
    return-void
.end method
