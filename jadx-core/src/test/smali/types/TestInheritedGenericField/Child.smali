.class public Ltypes/Child;
.super Ltypes/Middle;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ltypes/Middle<",
        "Ljava/lang/Integer;",
        ">;"
    }
.end annotation

.field private value:Ljava/lang/Integer;

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ltypes/Middle;-><init>()V
    return-void
.end method

.method public accept()V
    .registers 3

    iget-object v0, p0, Ltypes/Child;->callback:Ljava/util/function/Consumer;
    iget-object v1, p0, Ltypes/Child;->value:Ljava/lang/Integer;
    invoke-interface {v0, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    return-void
.end method
