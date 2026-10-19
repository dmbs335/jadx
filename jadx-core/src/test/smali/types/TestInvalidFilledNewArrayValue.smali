.class public Ltypes/TestInvalidFilledNewArrayValue;
.super Ljava/lang/Object;

.method public static getString()Ljava/lang/String;
    .locals 1
    const-string v0, "value"
    return-object v0
.end method

.method public static getInt()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static stringToIntArray()[I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidFilledNewArrayValue;->getString()Ljava/lang/String;
    move-result-object v0
    filled-new-array {v0}, [I
    move-result-object v0
    return-object v0
.end method

.method public static intToStringArray()[Ljava/lang/String;
    .locals 1
    invoke-static {}, Ltypes/TestInvalidFilledNewArrayValue;->getInt()I
    move-result v0
    filled-new-array {v0}, [Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method public static validStringArray()[Ljava/lang/String;
    .locals 1
    invoke-static {}, Ltypes/TestInvalidFilledNewArrayValue;->getString()Ljava/lang/String;
    move-result-object v0
    filled-new-array {v0}, [Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method public static varargs consumeStrings([Ljava/lang/String;)V
    .locals 0
    return-void
.end method

.method public static intToStringVarargs()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidFilledNewArrayValue;->getInt()I
    move-result v0
    filled-new-array {v0}, [Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Ltypes/TestInvalidFilledNewArrayValue;->consumeStrings([Ljava/lang/String;)V
    return-void
.end method
