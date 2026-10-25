.class public Linline/TestSyntheticBridgeSwappedArgs;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public bridge synthetic call(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    check-cast p1, Ljava/lang/String;
    check-cast p2, Ljava/lang/String;
    invoke-virtual {p0, p2, p1}, Linline/TestSyntheticBridgeSwappedArgs;->target(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method public target(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .registers 3
    return-object p1
.end method
