.class public Lconstructors/TestConstructorCheckedFieldPrefixOrderBlocked;
.super Lconstructors/TestConstructorCheckedFieldPrefixOrderBlockedParent;

.method public constructor <init>(Lconstructors/TestConstructorCheckedFieldPrefixOrderBlockedBuilder;)V
    .locals 2
    iget-object v0, p1, Lconstructors/TestConstructorCheckedFieldPrefixOrderBlockedBuilder;->value:Ljava/lang/Object;
    const-string v1, "builder.value"
    invoke-static {v0, v1}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-virtual {p1}, Lconstructors/TestConstructorCheckedFieldPrefixOrderBlockedBuilder;->getInput()Ljava/lang/Object;
    move-result-object v1
    invoke-direct {p0, v1, v0}, Lconstructors/TestConstructorCheckedFieldPrefixOrderBlockedParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
