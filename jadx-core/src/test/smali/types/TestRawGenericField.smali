.class public Ltypes/TestRawGenericField;
.super Ljava/lang/Object;

.field private final callback:Ljava/util/function/Function;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/function/Function<",
            "TT;",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.method public constructor <init>(Ljava/util/function/Function;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/function/Function<",
            "TT;",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Ltypes/TestRawGenericField;->callback:Ljava/util/function/Function;
    return-void
.end method

.method public invoke(Ljava/lang/Object;)Ljava/lang/String;
    .registers 3

    iget-object v0, p0, Ltypes/TestRawGenericField;->callback:Ljava/util/function/Function;
    invoke-interface {v0, p1}, Ljava/util/function/Function;->apply(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/String;
    return-object v0
.end method
