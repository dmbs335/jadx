.class public Ltypes/TestSiblingFieldPhi;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Ltypes/TestSiblingFieldPhi$Charging;,
        Ltypes/TestSiblingFieldPhi$Idle;,
        Ltypes/TestSiblingFieldPhi$Sink;,
        Ltypes/TestSiblingFieldPhi$Status;
    }
.end annotation

.field private final sink:Ltypes/TestSiblingFieldPhi$Sink;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ltypes/TestSiblingFieldPhi$Sink<",
            "Ltypes/TestSiblingFieldPhi$Status;>;"
        }
    .end annotation
.end field

.field private last:Ltypes/TestSiblingFieldPhi$Charging;

.method public constructor <init>(Ltypes/TestSiblingFieldPhi$Sink;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ltypes/TestSiblingFieldPhi$Sink<",
            "Ltypes/TestSiblingFieldPhi$Status;>;",
            ")V"
        }
    .end annotation

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Ltypes/TestSiblingFieldPhi;->sink:Ltypes/TestSiblingFieldPhi$Sink;
    return-void
.end method

.method public update(Z)V
    .registers 4

    iget-object v0, p0, Ltypes/TestSiblingFieldPhi;->sink:Ltypes/TestSiblingFieldPhi$Sink;

    if-eqz p1, :idle
    new-instance v1, Ltypes/TestSiblingFieldPhi$Charging;
    invoke-direct {v1}, Ltypes/TestSiblingFieldPhi$Charging;-><init>()V
    iput-object v1, p0, Ltypes/TestSiblingFieldPhi;->last:Ltypes/TestSiblingFieldPhi$Charging;
    move-object v2, v1
    goto :merge

    :idle
    sget-object v2, Ltypes/TestSiblingFieldPhi$Idle;->INSTANCE:Ltypes/TestSiblingFieldPhi$Idle;

    :merge
    invoke-interface {v0, v2}, Ltypes/TestSiblingFieldPhi$Sink;->setValue(Ljava/lang/Object;)V
    return-void
.end method
