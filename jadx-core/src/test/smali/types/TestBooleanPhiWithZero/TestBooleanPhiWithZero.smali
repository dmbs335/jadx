.class public Ltypes/TestBooleanPhiWithZero;
.super Ljava/lang/Object;

.method private static useInt(I)V
    .registers 1
    return-void
.end method

.method private static getZero()I
    .registers 1
    const/4 v0, 0x0
    return v0
.end method

.method public static test(Ltypes/TestBooleanPhiWithZero$Holder;Ltypes/TestBooleanPhiWithZero$Holder;)I
    .registers 5

    invoke-static {}, Ltypes/TestBooleanPhiWithZero;->getZero()I
    move-result v2
    invoke-static {v2}, Ltypes/TestBooleanPhiWithZero;->useInt(I)V
    if-eqz p0, :first_default
    invoke-virtual {p0}, Ltypes/TestBooleanPhiWithZero$Holder;->isReady()Z
    move-result v0
    goto :first_merge

    :first_default
    move v0, v2

    :first_merge
    if-eqz p1, :second_default
    invoke-virtual {p1}, Ltypes/TestBooleanPhiWithZero$Holder;->isReady()Z
    move-result v1
    goto :second_merge

    :second_default
    move v1, v2

    :second_merge
    or-int/2addr v0, v1
    if-eqz v0, :false_value
    const/4 v0, 0x1
    return v0

    :false_value
    const/4 v0, 0x0
    return v0
.end method
