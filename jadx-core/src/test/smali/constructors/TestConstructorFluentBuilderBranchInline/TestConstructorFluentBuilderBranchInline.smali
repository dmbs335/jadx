.class public Lconstructors/TestConstructorFluentBuilderBranchInline;
.super Ljava/lang/Exception;

.method public constructor <init>(I)V
    .locals 2

    invoke-static {p1}, Lconstructors/ConstructorFluentBuilderBranchHelper;->make(I)Ljava/lang/StringBuilder;
    move-result-object v0
    const/4 v1, 0x1
    if-le p1, v1, :singular
    const-string v1, "s"
    goto :append

    :singular
    const-string v1, ""

    :append
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    invoke-direct {p0, v0}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V
    return-void
.end method
