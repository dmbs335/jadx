.class public Ltypes/TestInvalidArrayType;
.super Ljava/lang/Object;

.method public static invalidNewArray(I)Ljava/lang/String;
    .locals 1
    new-array v0, p0, Ljava/lang/String;
    return-object v0
.end method

.method public static invalidFilledNewArray(Ljava/lang/String;)Ljava/lang/String;
    .locals 1
    filled-new-array {p0}, Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method public static validNewArray(I)[Ljava/lang/String;
    .locals 1
    new-array v0, p0, [Ljava/lang/String;
    return-object v0
.end method

.method public static validFilledNewArray(Ljava/lang/String;)[Ljava/lang/String;
    .locals 1
    filled-new-array {p0}, [Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method
