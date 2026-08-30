.class public Lconstructors/TestConstructorMethodArgWideningAliasInline;
.super Lconstructors/TestConstructorMethodArgWideningAliasParent;

.method public constructor <init>(Ljava/lang/String;Ljava/util/List;)V
    .locals 1

    check-cast p2, Ljava/util/Collection;
    invoke-interface {p2}, Ljava/util/Collection;->isEmpty()Z
    move-result v0
    if-eqz v0, :not_empty
    const/4 p2, 0x0

    :not_empty
    check-cast p2, Ljava/util/List;
    invoke-direct {p0, p2, p1}, Lconstructors/TestConstructorMethodArgWideningAliasParent;-><init>(Ljava/util/List;Ljava/lang/String;)V
    return-void
.end method
