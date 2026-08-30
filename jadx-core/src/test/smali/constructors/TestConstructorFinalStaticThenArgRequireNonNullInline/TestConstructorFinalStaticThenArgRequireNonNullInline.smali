.class public Lconstructors/TestConstructorFinalStaticThenArgRequireNonNullInline;
.super Lconstructors/TestConstructorFinalStaticThenArgRequireNonNullInlineParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 2
    sget-object v0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;
    const-string v1, "value"
    invoke-static {p1, v1}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-direct {p0, v0, p1}, Lconstructors/TestConstructorFinalStaticThenArgRequireNonNullInlineParent;-><init>(Ljava/util/List;Ljava/lang/String;)V
    return-void
.end method
