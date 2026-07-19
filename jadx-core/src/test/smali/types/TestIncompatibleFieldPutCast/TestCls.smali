.class public Ltypes/TestIncompatibleFieldPutCast;
.super Ljava/lang/Object;

.field private static sink:Ltypes/IncompatibleFieldPutMarker;

.method public static test()Ljava/lang/Object;
    .registers 1

    new-instance v0, Ltypes/IncompatibleFieldPutConcrete;
    invoke-direct {v0}, Ltypes/IncompatibleFieldPutConcrete;-><init>()V
    sput-object v0, Ltypes/TestIncompatibleFieldPutCast;->sink:Ltypes/IncompatibleFieldPutMarker;
    return-object v0
.end method
