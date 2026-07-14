.class public Ltypes/TestNullablePhiUseType;
.super Ljava/lang/Object;

.method private static useDefaults(Ltypes/TestNullablePhiUseType$Model;Ltypes/TestNullablePhiUseType$Marker;)V
    .registers 2
    return-void
.end method

.method private static getText()Ljava/lang/String;
    .registers 1
    const-string v0, "edit"
    return-object v0
.end method

.method private static useText(Ljava/lang/String;)V
    .registers 1
    return-void
.end method

.method public static test(ZZ)V
    .registers 4

    if-eqz p1, :second_null
    const/4 v0, 0x0
    goto :defaults

    :second_null
    const/4 v0, 0x0

    :defaults
    invoke-static {v0, v0}, Ltypes/TestNullablePhiUseType;->useDefaults(Ltypes/TestNullablePhiUseType$Model;Ltypes/TestNullablePhiUseType$Marker;)V

    if-eqz p0, :empty
    invoke-static {}, Ltypes/TestNullablePhiUseType;->getText()Ljava/lang/String;
    move-result-object v1
    goto :merge

    :empty
    move-object v1, v0

    :merge
    invoke-static {v1}, Ltypes/TestNullablePhiUseType;->useText(Ljava/lang/String;)V
    return-void
.end method
