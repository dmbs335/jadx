.class public Lconstructors/TestConstructorCheckedFieldPrefixInline;
.super Lconstructors/TestConstructorCheckedFieldPrefixInlineParent;

.method public constructor <init>(Lconstructors/TestConstructorCheckedFieldPrefixInlineBuilder;)V
    .locals 3
    invoke-virtual {p1}, Lconstructors/TestConstructorCheckedFieldPrefixInlineBuilder;->getInput()Ljava/lang/Object;
    move-result-object v0
    iget-object v1, p1, Lconstructors/TestConstructorCheckedFieldPrefixInlineBuilder;->value:Ljava/lang/Object;
    const-string v2, "builder.value"
    invoke-static {v1, v2}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    new-instance v2, Lconstructors/TestConstructorCheckedFieldPrefixInlinePair;
    invoke-direct {v2, v0, v1}, Lconstructors/TestConstructorCheckedFieldPrefixInlinePair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    invoke-direct {p0, v2, p1}, Lconstructors/TestConstructorCheckedFieldPrefixInlineParent;-><init>(Lconstructors/TestConstructorCheckedFieldPrefixInlinePair;Lconstructors/TestConstructorCheckedFieldPrefixInlineBuilder;)V
    return-void
.end method
