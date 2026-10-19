.class public Ltypes/TestInvalidFieldAssignment;
.super Ljava/lang/Object;

.field public static number:I
.field public static text:Ljava/lang/String;
.field public static any:Ljava/lang/Object;
.field public instanceNumber:I
.field public instanceText:Ljava/lang/String;

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

.method public static stringToNumber()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getString()Ljava/lang/String;
    move-result-object v0
    sput v0, Ltypes/TestInvalidFieldAssignment;->number:I
    return-void
.end method

.method public static intToString()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getInt()I
    move-result v0
    sput-object v0, Ltypes/TestInvalidFieldAssignment;->text:Ljava/lang/String;
    return-void
.end method

.method public static objectToString()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getObject()Ljava/lang/Object;
    move-result-object v0
    sput-object v0, Ltypes/TestInvalidFieldAssignment;->text:Ljava/lang/String;
    return-void
.end method

.method public static stringToInstanceNumber(Ltypes/TestInvalidFieldAssignment;)V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getString()Ljava/lang/String;
    move-result-object v0
    iput v0, p0, Ltypes/TestInvalidFieldAssignment;->instanceNumber:I
    return-void
.end method

.method public static intToInstanceString(Ltypes/TestInvalidFieldAssignment;)V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getInt()I
    move-result v0
    iput-object v0, p0, Ltypes/TestInvalidFieldAssignment;->instanceText:Ljava/lang/String;
    return-void
.end method

.method public static validStringToAny()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getString()Ljava/lang/String;
    move-result-object v0
    sput-object v0, Ltypes/TestInvalidFieldAssignment;->any:Ljava/lang/Object;
    return-void
.end method

.method public static validIntToAny()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getInt()I
    move-result v0
    sput-object v0, Ltypes/TestInvalidFieldAssignment;->any:Ljava/lang/Object;
    return-void
.end method

.method public static validStringToText()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidFieldAssignment;->getString()Ljava/lang/String;
    move-result-object v0
    sput-object v0, Ltypes/TestInvalidFieldAssignment;->text:Ljava/lang/String;
    return-void
.end method
