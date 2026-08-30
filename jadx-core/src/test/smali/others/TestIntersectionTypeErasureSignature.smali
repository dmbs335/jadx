.class public Lothers/TestIntersectionTypeErasureSignature;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<T:Ljava/lang/Object;",
        "B:Ljava/util/ArrayList<TT;>;:Ljava/io/Serializable;",
        "C:TB;>",
        "Ljava/lang/Object;"
    }
.end annotation

.method public acceptClass(Ljava/util/ArrayList;)V
    .registers 2

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TB;)V"
        }
    .end annotation

    return-void
.end method

.method public static acceptMethod(Ljava/util/List;)V
    .registers 1

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<E:Ljava/lang/Object;",
            "R::Ljava/util/List<TE;>;:Ljava/io/Serializable;>",
            "(TR;)V"
        }
    .end annotation

    return-void
.end method

.method public returnClass()Ljava/util/ArrayList;
    .registers 1

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()TB;"
        }
    .end annotation

    const/4 v0, 0x0
    return-object v0
.end method

.method public returnChained()Ljava/util/ArrayList;
    .registers 1

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()TC;"
        }
    .end annotation

    const/4 v0, 0x0
    return-object v0
.end method

.method public static returnMethod()Ljava/util/List;
    .registers 1

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<E:Ljava/lang/Object;",
            "R::Ljava/util/List<TE;>;:Ljava/io/Serializable;>",
            "()TR;"
        }
    .end annotation

    const/4 v0, 0x0
    return-object v0
.end method

.method public static forwardTypeVariableBound(Ljava/util/Map;Ljava/util/function/Supplier;)Ljava/lang/Object;
    .registers 3

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<M::Ljava/util/Map<**>;:TR;R:Ljava/lang/Object;>",
            "(TM;Ljava/util/function/Supplier<+TR;>;)",
            "TR;"
        }
    .end annotation

    invoke-interface {p1}, Ljava/util/function/Supplier;->get()Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
