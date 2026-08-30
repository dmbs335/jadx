.class public Lconstructors/TestConstructorMethodArgAliasInline;
.super Lconstructors/TestConstructorMethodArgAliasParent;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 1

    move-object v0, p1
    invoke-direct {p0, v0, v0}, Lconstructors/TestConstructorMethodArgAliasParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
