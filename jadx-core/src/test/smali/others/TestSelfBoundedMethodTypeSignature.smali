.class public Lothers/TestSelfBoundedMethodTypeSignature;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<K:Ljava/lang/Object;V:Ljava/lang/Object;>",
        "Ljava/lang/Object;"
    }
.end annotation

.method public static of(Ljava/lang/Comparable;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<K::Ljava/lang/Comparable<-TK;>;V:Ljava/lang/Object;>",
            "(TK;TV;)",
            "TV;"
        }
    .end annotation

    return-object p1
.end method
