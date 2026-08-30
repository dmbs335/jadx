.class public Lconstructors/TestSuperRequireNonNullInline;
.super Lconstructors/TestSuperRequireNonNullInlineParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 2
    const-string v0, "value"
    invoke-static {p1, v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v1
    invoke-direct {p0, v1}, Lconstructors/TestSuperRequireNonNullInlineParent;-><init>(I)V
    return-void
.end method
