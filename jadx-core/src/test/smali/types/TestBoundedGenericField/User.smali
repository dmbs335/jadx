.class public Ltypes/User;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<K:",
        "Ljava/lang/Comparable;",
        ">",
        "Ljava/lang/Object;"
    }
.end annotation

.field private box:Ltypes/Box;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ltypes/Box<",
            "TK;",
            ">;"
        }
    .end annotation
.end field

.field private value:Ljava/lang/Comparable;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "TK;"
        }
    .end annotation
.end field

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public accept()V
    .registers 4

    iget-object v0, p0, Ltypes/User;->box:Ltypes/Box;
    iget-object v1, v0, Ltypes/Box;->callback:Ljava/util/function/Consumer;
    iget-object v2, p0, Ltypes/User;->value:Ljava/lang/Comparable;
    invoke-interface {v1, v2}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    return-void
.end method
