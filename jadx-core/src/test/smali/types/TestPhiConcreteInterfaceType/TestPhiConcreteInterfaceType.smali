.class public Ltypes/TestPhiConcreteInterfaceType;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/Object;Z)V
    .registers 5

    const/4 v2, 0x0

    if-eqz p1, :null_value

    invoke-static {p0}, Ltypes/TestPhiConcreteInterfaceType$Source;->first(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    move-object v1, v0
    check-cast v1, Ltypes/TestPhiConcreteInterfaceType$Both;
    move-object v0, v1
    goto :merge

    :null_value
    move-object v0, v2

    :merge
    if-eqz v0, :end
    invoke-static {v0}, Ltypes/TestPhiConcreteInterfaceType$Sink;->useOwner(Ltypes/TestPhiConcreteInterfaceType$Owner;)V
    invoke-interface {v0}, Ltypes/TestPhiConcreteInterfaceType$HasExtras;->getExtras()Ljava/lang/Object;

    :end
    invoke-static {v2}, Ltypes/TestPhiConcreteInterfaceType$Sink;->useContext(Ltypes/TestPhiConcreteInterfaceType$Context;)V
    return-void
.end method

.method public static testUnknown(Ljava/lang/Object;Z)V
    .registers 5

    const/4 v2, 0x0
    if-eqz p1, :null_value
    invoke-static {p0}, Ltypes/TestPhiConcreteInterfaceType$Source;->first(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    goto :merge

    :null_value
    move-object v0, v2

    :merge
    if-eqz v0, :end
    invoke-static {v0}, Ltypes/TestPhiConcreteInterfaceType$Sink;->useOwner(Ltypes/TestPhiConcreteInterfaceType$Owner;)V
    invoke-interface {v0}, Ltypes/TestPhiConcreteInterfaceType$HasExtras;->getExtras()Ljava/lang/Object;

    :end
    invoke-static {v2}, Ltypes/TestPhiConcreteInterfaceType$Sink;->useContext(Ltypes/TestPhiConcreteInterfaceType$Context;)V
    return-void
.end method
