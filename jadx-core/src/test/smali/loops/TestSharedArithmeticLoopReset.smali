.class public Lloops/TestSharedArithmeticLoopReset;
.super Ljava/lang/Object;

.method public static find(Ljava/util/Map;)Ljava/util/Map;
    .locals 5

    const/4 v3, 0x0
    const/4 v2, 0x0
    const-string v4, "parent"
    invoke-interface {p0, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/Map;
    if-eqz v0, :advance

    const-string v4, "config"
    invoke-interface {v0, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Ljava/util/Map;
    if-eqz v1, :advance

    const-string v4, "action"
    invoke-interface {v1, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    goto :header

    :header
    const/16 v4, 0xa
    if-gt v3, v4, :exit
    if-eqz v0, :exit
    if-nez v2, :exit

    const-string v4, "parent"
    invoke-interface {v0, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/Map;
    if-eqz v0, :advance

    const-string v4, "config"
    invoke-interface {v0, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Ljava/util/Map;
    if-eqz v1, :advance

    const-string v4, "action"
    invoke-interface {v1, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    goto :header

    :advance
    add-int/lit8 v3, v3, 0x1
    goto :header

    :exit
    return-object v0
.end method
