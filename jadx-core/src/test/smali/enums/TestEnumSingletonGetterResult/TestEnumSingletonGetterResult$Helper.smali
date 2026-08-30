.class public final Lenums/TestEnumSingletonGetterResult$Helper;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lenums/TestEnumSingletonGetterResult$Helper;

.method static constructor <clinit>()V
    .registers 1
    new-instance v0, Lenums/TestEnumSingletonGetterResult$Helper;
    invoke-direct {v0}, Lenums/TestEnumSingletonGetterResult$Helper;-><init>()V
    sput-object v0, Lenums/TestEnumSingletonGetterResult$Helper;->INSTANCE:Lenums/TestEnumSingletonGetterResult$Helper;
    return-void
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public final getValue()Ljava/lang/String;
    .registers 1
    const-string v0, "value"
    return-object v0
.end method
