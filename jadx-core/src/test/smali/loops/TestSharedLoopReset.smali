.class public Lloops/TestSharedLoopReset;
.super Ljava/lang/Object;

.method public static find(Ljava/util/Map;)Ljava/util/Map;
    .locals 4

    const-string v3, "parent"
    invoke-interface {p0, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/Map;
    if-eqz v0, :reset

    const-string v3, "config"
    invoke-interface {v0, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Ljava/util/Map;
    if-eqz v1, :reset

    const-string v3, "action"
    invoke-interface {v1, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    goto :header

    :header
    if-eqz v0, :exit
    if-nez v2, :exit

    const-string v3, "parent"
    invoke-interface {v0, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/Map;
    if-eqz v0, :reset

    const-string v3, "config"
    invoke-interface {v0, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Ljava/util/Map;
    if-eqz v1, :reset

    const-string v3, "action"
    invoke-interface {v1, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    goto :header

    :reset
    const/4 v2, 0x0
    goto :header

    :exit
    return-object v0
.end method
