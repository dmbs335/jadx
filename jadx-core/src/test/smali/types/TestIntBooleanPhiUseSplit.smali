.class public Ltypes/TestIntBooleanPhiUseSplit;
.super Ljava/lang/Object;

.method private static acceptBoolean(Z)V
    .registers 1
    return-void
.end method

.method private static acceptInt(I)V
    .registers 1
    return-void
.end method

.method public static test(ZI)V
	.registers 4

	if-eqz p0, :false_branch
	move v0, p1
    goto :merge

    :false_branch
    const/4 v0, 0x0

    :merge
    move v1, v0
    invoke-static {v0}, Ltypes/TestIntBooleanPhiUseSplit;->acceptBoolean(Z)V
    or-int/lit8 v1, v1, 0x8
    invoke-static {v1}, Ltypes/TestIntBooleanPhiUseSplit;->acceptInt(I)V
	return-void
.end method

.method public static testArrayIndex(ZI[Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 6

    if-eqz p0, :array_false_branch
    move v0, p1
    goto :array_merge

    :array_false_branch
    const/4 v0, 0x0

    :array_merge
    invoke-static {v0}, Ltypes/TestIntBooleanPhiUseSplit;->acceptBoolean(Z)V
    aput-object p3, p2, v0
    return-void
.end method
