.class public Lconstructors/TestConstructorFinalSingletonInline;
.super Lconstructors/TestConstructorFinalSingletonInlineParent;

.field private final singleton:Lconstructors/ConstructorFinalSingleton;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 1

    sget-object v0, Lconstructors/ConstructorFinalSingleton;->INSTANCE:Lconstructors/ConstructorFinalSingleton;
    invoke-direct {p0, p1, v0, v0}, Lconstructors/TestConstructorFinalSingletonInlineParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    iput-object v0, p0, Lconstructors/TestConstructorFinalSingletonInline;->singleton:Lconstructors/ConstructorFinalSingleton;
    return-void
.end method
