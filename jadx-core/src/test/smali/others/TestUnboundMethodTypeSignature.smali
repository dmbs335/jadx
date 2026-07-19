.class public Lothers/TestUnboundMethodTypeSignature;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<B:Ljava/lang/Object;>",
        "Ljava/lang/Object;"
    }
.end annotation

.method public accept(Ljava/lang/String;Ljava/util/function/Consumer;)V
    .registers 3

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TT;",
            "Ljava/util/function/Consumer<",
            "-Lothers/TestUnboundMethodTypeSignature;>;)",
            "V"
        }
    .end annotation

    return-void
.end method

.method public acceptKnown(Ljava/lang/String;)V
    .registers 2

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TB;)V"
        }
    .end annotation

    return-void
.end method

.method public returnValue()Ljava/lang/String;
    .registers 1

    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()TT;"
        }
    .end annotation

    const/4 v0, 0x0
    return-object v0
.end method
