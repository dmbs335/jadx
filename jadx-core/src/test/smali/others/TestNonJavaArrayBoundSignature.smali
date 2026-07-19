.class public Lothers/TestNonJavaArrayBoundSignature;
.super Ljava/lang/Object;

.method public static forwardArrayTypeVariableBound([Ljava/lang/Object;Ljava/util/function/Supplier;)Ljava/lang/Object;
    .registers 3

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<C:[Ljava/lang/Object;:TR;R:Ljava/lang/Object;>",
            "(TC;Ljava/util/function/Supplier<+TR;>;)",
            "TR;"
        }
    .end annotation

    invoke-interface {p1}, Ljava/util/function/Supplier;->get()Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
