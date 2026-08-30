.class public Lloops/TestIteratorRegisterSubtypeBranches;
.super Ljava/lang/Object;

.method private static convertCircular(Llegacy/CircularGeofence;)Ljava/lang/Object;
    .registers 2
    const/4 v0, 0x0
    return-object v0
.end method

.method private static convertPolygonal(Llegacy/PolygonalGeofence;)Ljava/lang/Object;
    .registers 2
    const/4 v0, 0x0
    return-object v0
.end method

.method private static convertLinear(Llegacy/LinearGeofence;)Ljava/lang/Object;
    .registers 2
    const/4 v0, 0x0
    return-object v0
.end method

.method public static convert(Ljava/util/List;)Ljava/util/List;
    .registers 6
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List<",
            "Llegacy/Geofence;",
            ">;)",
            "Ljava/util/List<",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation

    const/4 v0, 0x0
    if-nez p0, :non_null
    return-object v0

    :non_null
    new-instance v1, Ljava/util/ArrayList;
    invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object p0

    :loop
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :end
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, Llegacy/Geofence;

    iget v3, v2, Llegacy/Geofence;->type:I
    const/4 v4, 0x1
    if-ne v3, v4, :polygonal
    check-cast v2, Llegacy/CircularGeofence;
    invoke-static {v2}, Lloops/TestIteratorRegisterSubtypeBranches;->convertCircular(Llegacy/CircularGeofence;)Ljava/lang/Object;
    move-result-object v2
    goto :add

    :polygonal
    iget v3, v2, Llegacy/Geofence;->type:I
    const/4 v4, 0x2
    if-ne v3, v4, :linear
    check-cast v2, Llegacy/PolygonalGeofence;
    invoke-static {v2}, Lloops/TestIteratorRegisterSubtypeBranches;->convertPolygonal(Llegacy/PolygonalGeofence;)Ljava/lang/Object;
    move-result-object v2
    goto :add

    :linear
    iget v3, v2, Llegacy/Geofence;->type:I
    const/4 v4, 0x3
    if-ne v3, v4, :unknown
    check-cast v2, Llegacy/LinearGeofence;
    invoke-static {v2}, Lloops/TestIteratorRegisterSubtypeBranches;->convertLinear(Llegacy/LinearGeofence;)Ljava/lang/Object;
    move-result-object v2
    goto :add

    :unknown
    move-object v2, v0

    :add
    invoke-interface {v1, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    goto :loop

    :end
    return-object v1
.end method
