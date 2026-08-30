.class public Ltypes/TestClosedObjectPhiLoop;
.super Ljava/lang/Object;

.method private static next(Landroidx/compose/ui/Modifier$Node;)Landroidx/compose/ui/Modifier$Node;
    .registers 1
    return-object p0
.end method

.method public static collect(Landroidx/compose/ui/Modifier$Node;Ljava/util/List;I)Landroidx/compose/ui/Modifier$Node;
    .registers 6

    move-object v0, p0

    :outer
    invoke-static {v0}, Ltypes/TestClosedObjectPhiLoop;->next(Landroidx/compose/ui/Modifier$Node;)Landroidx/compose/ui/Modifier$Node;
    move-result-object v1
    move-object v0, v1

    :inner
    if-eqz v0, :inner_done
    invoke-interface {p1, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    if-eqz p2, :set_null
    invoke-static {v0}, Ltypes/TestClosedObjectPhiLoop;->next(Landroidx/compose/ui/Modifier$Node;)Landroidx/compose/ui/Modifier$Node;
    move-result-object v0
    goto :inner

    :set_null
    const/4 v0, 0x0
    goto :inner

    :inner_done
    if-lez p2, :return
    add-int/lit8 p2, p2, -0x1
    invoke-static {p0}, Ltypes/TestClosedObjectPhiLoop;->next(Landroidx/compose/ui/Modifier$Node;)Landroidx/compose/ui/Modifier$Node;
    move-result-object v0
    goto :outer

    :return
    return-object v0
.end method
