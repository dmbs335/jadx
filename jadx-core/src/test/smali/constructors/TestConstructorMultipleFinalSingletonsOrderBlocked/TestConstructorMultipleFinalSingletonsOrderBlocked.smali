.class public Lconstructors/TestConstructorMultipleFinalSingletonsOrderBlocked;
.super Lconstructors/TestConstructorMultipleFinalSingletonsOrderBlockedParent;

.method public constructor <init>()V
    .locals 2
    sget-object v0, Lconstructors/ConstructorMultiFinalOrderA;->INSTANCE:Lconstructors/ConstructorMultiFinalOrderA;
    sget-object v1, Lconstructors/ConstructorMultiFinalOrderB;->INSTANCE:Lconstructors/ConstructorMultiFinalOrderB;
    invoke-direct {p0, v1, v0, v1, v0}, Lconstructors/TestConstructorMultipleFinalSingletonsOrderBlockedParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method
