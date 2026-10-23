.class public Ltypes/TestInvalidAbstractAllocation;
.super Ljava/lang/Object;

.method public static createAbstract()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ltypes/TestInvalidAbstractAllocation$AbstractTarget;
    invoke-direct {v0}, Ltypes/TestInvalidAbstractAllocation$AbstractTarget;-><init>()V
    return-object v0
.end method

.method public static createConcrete()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ltypes/TestInvalidAbstractAllocation$ConcreteTarget;
    invoke-direct {v0}, Ltypes/TestInvalidAbstractAllocation$ConcreteTarget;-><init>()V
    return-object v0
.end method

.method public static createInterface()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ltypes/TestInvalidAbstractAllocation$InterfaceTarget;
    return-object v0
.end method
