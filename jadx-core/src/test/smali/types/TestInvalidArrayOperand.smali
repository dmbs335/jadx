.class public Ltypes/TestInvalidArrayOperand;
.super Ljava/lang/Object;

.field public static text:Ljava/lang/String;

.method public static length()I
    .locals 2

    const/4 v0, 0x1
    array-length v1, v0
    return v1
.end method

.method public static get()I
    .locals 3

    const/4 v0, 0x1
    const/4 v1, 0x0
    aget v2, v0, v1
    return v2
.end method

.method public static put()V
    .locals 3

    const/4 v0, 0x1
    const/4 v1, 0x0
    const/4 v2, 0x7
    aput v2, v0, v1
    return-void
.end method

.method public static nullLength()I
    .locals 2

    const/4 v0, 0x0
    array-length v1, v0
    return v1
.end method

.method public static nullGet()I
    .locals 3

    const/4 v0, 0x0
    const/4 v1, 0x0
    aget v2, v0, v1
    return v2
.end method

.method public static lengthParam(I)I
    .locals 1

    array-length v0, p0
    return v0
.end method

.method public static getParam(I)I
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    return v1
.end method

.method public static putParam(I)V
    .locals 2

    const/4 v0, 0x0
    const/4 v1, 0x7
    aput v1, p0, v0
    return-void
.end method

.method public static lengthObject(Ljava/lang/Object;)I
    .locals 1

    array-length v0, p0
    return v0
.end method

.method public static getObject(Ljava/lang/Object;)I
    .locals 2

    const/4 v0, 0x0
    aget v1, p0, v0
    return v1
.end method

.method public static putObject(Ljava/lang/Object;)V
    .locals 2

    const/4 v0, 0x0
    const/4 v1, 0x7
    aput v1, p0, v0
    return-void
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "text"
    return-object v0
.end method

.method public static lengthInvoke()I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayOperand;->getString()Ljava/lang/String;
    move-result-object v0
    array-length v1, v0
    return v1
.end method

.method public static getInvoke()I
    .locals 3

    invoke-static {}, Ltypes/TestInvalidArrayOperand;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    aget v2, v0, v1
    return v2
.end method

.method public static putInvoke()V
    .locals 3

    invoke-static {}, Ltypes/TestInvalidArrayOperand;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    const/4 v2, 0x7
    aput v2, v0, v1
    return-void
.end method

.method public static lengthField()I
    .locals 2

    sget-object v0, Ltypes/TestInvalidArrayOperand;->text:Ljava/lang/String;
    array-length v1, v0
    return v1
.end method
