.class public final Lconstructors/ConstructorFinalSingletonCondition;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lconstructors/ConstructorFinalSingletonCondition;

.method static constructor <clinit>()V
    .locals 1

    new-instance v0, Lconstructors/ConstructorFinalSingletonCondition;
    invoke-direct {v0}, Lconstructors/ConstructorFinalSingletonCondition;-><init>()V
    sput-object v0, Lconstructors/ConstructorFinalSingletonCondition;->INSTANCE:Lconstructors/ConstructorFinalSingletonCondition;
    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public final isFirst()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public final isSecond()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method
