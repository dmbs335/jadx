.class public Ltypes/TestInvalidReferenceCompare;
.super Ljava/lang/Object;

.method public static unrelatedFinalClasses(Ljava/lang/String;Ljava/lang/Integer;)Z
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method

.method public static stringAndObject(Ljava/lang/String;Ljava/lang/Object;)Z
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1
    const-string v0, "value"
    return-object v0
.end method

.method public static getInteger()Ljava/lang/Integer;
    .locals 1
    const/4 v0, 0x1
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    return-object v0
.end method

.method public static unrelatedInvokeResults()Z
    .locals 3
    invoke-static {}, Ltypes/TestInvalidReferenceCompare;->getString()Ljava/lang/String;
    move-result-object v0
    invoke-static {}, Ltypes/TestInvalidReferenceCompare;->getInteger()Ljava/lang/Integer;
    move-result-object v1
    if-ne v0, v1, :different
    const/4 v2, 0x0
    return v2
    :different
    const/4 v2, 0x1
    return v2
.end method

.method public static stringAndNull(Ljava/lang/String;)Z
    .locals 1
    if-eqz p0, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method

.method public static relationalStrings(Ljava/lang/String;Ljava/lang/String;)Z
    .locals 1
    if-lt p0, p1, :less
    const/4 v0, 0x0
    return v0
    :less
    const/4 v0, 0x1
    return v0
.end method

.method public static relationalStringZero(Ljava/lang/String;)Z
    .locals 1
    if-gez p0, :nonnegative
    const/4 v0, 0x0
    return v0
    :nonnegative
    const/4 v0, 0x1
    return v0
.end method

.method public static referenceAndPrimitive(Ljava/lang/String;I)Z
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method

.method public static unrelatedClasses(Ljava/lang/Number;Ljava/lang/Thread;)Z
    .locals 1
    if-ne p0, p1, :different
    const/4 v0, 0x0
    return v0
    :different
    const/4 v0, 0x1
    return v0
.end method

.method public static unrelatedArrays([I[J)Z
    .locals 1
    if-eq p0, p1, :equal
    const/4 v0, 0x0
    return v0
    :equal
    const/4 v0, 0x1
    return v0
.end method
