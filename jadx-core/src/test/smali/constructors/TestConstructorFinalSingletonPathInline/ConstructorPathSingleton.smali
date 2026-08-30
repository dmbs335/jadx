.class public final Lconstructors/ConstructorPathSingleton;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lconstructors/ConstructorPathSingleton;

.method static constructor <clinit>()V
    .locals 1

    new-instance v0, Lconstructors/ConstructorPathSingleton;
    invoke-direct {v0}, Lconstructors/ConstructorPathSingleton;-><init>()V
    sput-object v0, Lconstructors/ConstructorPathSingleton;->INSTANCE:Lconstructors/ConstructorPathSingleton;
    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public transform(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0

    return-object p1
.end method

.method public getValue()Ljava/lang/Object;
    .locals 0

    return-object p0
.end method
