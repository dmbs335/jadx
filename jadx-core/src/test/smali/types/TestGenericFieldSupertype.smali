.class public Ltypes/TestGenericFieldSupertype;
.super Ljava/lang/Object;

.field private final values:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "TT;",
            ">;"
        }
    .end annotation
.end field

.method public constructor <init>(Ljava/util/ArrayList;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/ArrayList<",
            "TT;",
            ">;)V"
        }
    .end annotation

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Ltypes/TestGenericFieldSupertype;->values:Ljava/util/ArrayList;
    return-void
.end method

.method public add(Ljava/lang/Object;)Z
    .registers 3

    iget-object v0, p0, Ltypes/TestGenericFieldSupertype;->values:Ljava/util/ArrayList;
    invoke-interface {v0, p1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    move-result v0
    return v0
.end method
