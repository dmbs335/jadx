.class public final Lconstructors/ConstructorFinalSingletonConditionalRead;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lconstructors/ConstructorFinalSingletonConditionalRead;

.method static constructor <clinit>()V
    .locals 1

    new-instance v0, Lconstructors/ConstructorFinalSingletonConditionalRead;
    invoke-direct {v0}, Lconstructors/ConstructorFinalSingletonConditionalRead;-><init>()V
    sput-object v0, Lconstructors/ConstructorFinalSingletonConditionalRead;->INSTANCE:Lconstructors/ConstructorFinalSingletonConditionalRead;
    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public final isEnabled()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public final isOtherEnabled()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method
