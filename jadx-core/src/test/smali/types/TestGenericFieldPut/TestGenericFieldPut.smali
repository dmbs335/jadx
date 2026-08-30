.class public Ltypes/TestGenericFieldPut;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/Object;)Ltypes/TestGenericFieldPut$Box;
    .registers 3
    new-instance v0, Ltypes/TestGenericFieldPut$Box;
    invoke-direct {v0}, Ltypes/TestGenericFieldPut$Box;-><init>()V
    invoke-static {p0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v1
    iput-object v1, v0, Ltypes/TestGenericFieldPut$Box;->value:Ljava/lang/Object;
    return-object v0
.end method
