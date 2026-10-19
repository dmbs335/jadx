.class public Ltypes/TestInvalidReturnType;
.super Ljava/lang/Object;

.method public static objectToInt(Ljava/lang/Object;)I
    .locals 0

    return p0
.end method

.method public static stringToInt(Ljava/lang/String;)I
    .locals 0

    return p0
.end method

.method public static intToString(I)Ljava/lang/String;
    .locals 0

    return-object p0
.end method

.method public static intToObject(I)Ljava/lang/Object;
    .locals 0

    return-object p0
.end method

.method public static validInt(I)I
    .locals 0

    return p0
.end method

.method public static validString(Ljava/lang/String;)Ljava/lang/String;
    .locals 0

    return-object p0
.end method

.method public static missingValue()I
    .locals 0

    return-void
.end method

.method public static validVoid()V
    .locals 0

    return-void
.end method

.method public static valueInVoid(I)V
    .locals 0

    return p0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "text"
    return-object v0
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static getObject()Ljava/lang/Object;
    .locals 1

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method

.method public static stringInvokeToInt()I
    .locals 1

    invoke-static {}, Ltypes/TestInvalidReturnType;->getString()Ljava/lang/String;
    move-result-object v0
    return v0
.end method

.method public static intInvokeToString()Ljava/lang/String;
    .locals 1

    invoke-static {}, Ltypes/TestInvalidReturnType;->getInt()I
    move-result v0
    return-object v0
.end method

.method public static objectInvokeToString()Ljava/lang/String;
    .locals 1

    invoke-static {}, Ltypes/TestInvalidReturnType;->getObject()Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static stringInvokeToObject()Ljava/lang/Object;
    .locals 1

    invoke-static {}, Ltypes/TestInvalidReturnType;->getString()Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method public static intInvokeToObject()Ljava/lang/Object;
    .locals 1

    invoke-static {}, Ltypes/TestInvalidReturnType;->getInt()I
    move-result v0
    return-object v0
.end method
