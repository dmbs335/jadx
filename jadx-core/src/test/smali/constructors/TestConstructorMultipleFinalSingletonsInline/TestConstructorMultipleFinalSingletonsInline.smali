.class public Lconstructors/TestConstructorMultipleFinalSingletonsInline;
.super Lconstructors/TestConstructorMultipleFinalSingletonsInlineParent;

.method public constructor <init>()V
    .locals 2
    sget-object v0, Lconstructors/ConstructorMultiFinalA;->INSTANCE:Lconstructors/ConstructorMultiFinalA;
    sget-object v1, Lconstructors/ConstructorMultiFinalB;->INSTANCE:Lconstructors/ConstructorMultiFinalB;
    invoke-direct {p0, v0, v1, v0}, Lconstructors/TestConstructorMultipleFinalSingletonsInlineParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
