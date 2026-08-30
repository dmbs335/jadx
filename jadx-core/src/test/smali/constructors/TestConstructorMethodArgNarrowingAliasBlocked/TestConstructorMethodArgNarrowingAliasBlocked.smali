.class public Lconstructors/TestConstructorMethodArgNarrowingAliasBlocked;
.super Lconstructors/TestConstructorMethodArgNarrowingAliasParent;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 1

    move-object v0, p1
    check-cast v0, Ljava/util/List;
    invoke-direct {p0, v0, v0}, Lconstructors/TestConstructorMethodArgNarrowingAliasParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
