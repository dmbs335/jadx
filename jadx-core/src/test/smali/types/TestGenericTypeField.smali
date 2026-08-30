.class public Ltypes/TestGenericTypeField;
.super Ljava/lang/Object;

.field private final sink:Ljava/util/function/Consumer;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/function/Consumer<",
            "TT;",
            ">;"
        }
    .end annotation
.end field

.field private final value:Ljava/lang/Object;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "TT;"
        }
    .end annotation
.end field

.method public constructor <init>(Ljava/util/function/Consumer;Ljava/lang/Object;)V
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/function/Consumer<",
            "TT;",
            ">;",
            "TT;)V"
        }
    .end annotation

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Ltypes/TestGenericTypeField;->sink:Ljava/util/function/Consumer;
    iput-object p2, p0, Ltypes/TestGenericTypeField;->value:Ljava/lang/Object;
    return-void
.end method

.method public accept()V
    .registers 3

    iget-object v0, p0, Ltypes/TestGenericTypeField;->sink:Ljava/util/function/Consumer;
    iget-object v1, p0, Ltypes/TestGenericTypeField;->value:Ljava/lang/Object;
    invoke-interface {v0, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    return-void
.end method
