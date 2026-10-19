.class public Ltypes/TestInvalidArrayPutValue;
.super Ljava/lang/Object;

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

.method public static stringToIntArray([I)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    aput v0, p0, v1
    return-void
.end method

.method public static intToStringArray([Ljava/lang/String;)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getInt()I
    move-result v0
    const/4 v1, 0x0
    aput-object v0, p0, v1
    return-void
.end method

.method public static objectToStringArray([Ljava/lang/String;)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getObject()Ljava/lang/Object;
    move-result-object v0
    const/4 v1, 0x0
    aput-object v0, p0, v1
    return-void
.end method

.method public static validIntToObjectArray([Ljava/lang/Object;)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getInt()I
    move-result v0
    const/4 v1, 0x0
    aput-object v0, p0, v1
    return-void
.end method

.method public static validStringToObjectArray([Ljava/lang/Object;)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    aput-object v0, p0, v1
    return-void
.end method

.method public static validStringToStringArray([Ljava/lang/String;)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayPutValue;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x0
    aput-object v0, p0, v1
    return-void
.end method
