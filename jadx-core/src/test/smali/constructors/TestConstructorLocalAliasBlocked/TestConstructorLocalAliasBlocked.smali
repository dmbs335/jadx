.class public Lconstructors/TestConstructorLocalAliasBlocked;
.super Lconstructors/TestConstructorLocalAliasParent;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 2

    invoke-static {p1}, Lconstructors/ConstructorLocalAliasHelper;->make(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    move-object v1, v0
    invoke-direct {p0, v1, v1}, Lconstructors/TestConstructorLocalAliasParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
