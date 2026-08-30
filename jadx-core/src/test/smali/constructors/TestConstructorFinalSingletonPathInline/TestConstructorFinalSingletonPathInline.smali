.class public Lconstructors/TestConstructorFinalSingletonPathInline;
.super Lconstructors/TestConstructorFinalSingletonPathInlineParent;

.method public constructor <init>(Ljava/lang/Object;Z)V
    .locals 3

    sget-object v0, Lconstructors/ConstructorPathSingleton;->INSTANCE:Lconstructors/ConstructorPathSingleton;
    invoke-virtual {v0, p1}, Lconstructors/ConstructorPathSingleton;->transform(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    if-eqz p2, :null_value
    invoke-virtual {v0}, Lconstructors/ConstructorPathSingleton;->getValue()Ljava/lang/Object;
    move-result-object v2
    goto :invoke_constructor

    :null_value
    const/4 v2, 0x0

    :invoke_constructor
    invoke-direct {p0, v1, v2}, Lconstructors/TestConstructorFinalSingletonPathInlineParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
