.class public final Lconstructors/ConstructorFinalSingleton;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lconstructors/ConstructorFinalSingleton;

.method static constructor <clinit>()V
    .locals 1

    new-instance v0, Lconstructors/ConstructorFinalSingleton;
    invoke-direct {v0}, Lconstructors/ConstructorFinalSingleton;-><init>()V
    sput-object v0, Lconstructors/ConstructorFinalSingleton;->INSTANCE:Lconstructors/ConstructorFinalSingleton;
    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
