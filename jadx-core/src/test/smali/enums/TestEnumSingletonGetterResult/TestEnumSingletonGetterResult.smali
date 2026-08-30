.class public final enum Lenums/TestEnumSingletonGetterResult;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSingletonGetterResult;
.field public static final enum ONE:Lenums/TestEnumSingletonGetterResult;
.field private final value:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 5

    sget-object v0, Lenums/TestEnumSingletonGetterResult$Helper;->INSTANCE:Lenums/TestEnumSingletonGetterResult$Helper;
    invoke-virtual {v0}, Lenums/TestEnumSingletonGetterResult$Helper;->getValue()Ljava/lang/String;
    move-result-object v1
    new-instance v2, Lenums/TestEnumSingletonGetterResult;
    const-string v3, "ONE"
    const/4 v4, 0x0
    invoke-direct {v2, v3, v4, v1}, Lenums/TestEnumSingletonGetterResult;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    sput-object v2, Lenums/TestEnumSingletonGetterResult;->ONE:Lenums/TestEnumSingletonGetterResult;

    sget-object v0, Lenums/TestEnumSingletonGetterResult;->ONE:Lenums/TestEnumSingletonGetterResult;
    filled-new-array {v0}, [Lenums/TestEnumSingletonGetterResult;
    move-result-object v0
    sput-object v0, Lenums/TestEnumSingletonGetterResult;->$VALUES:[Lenums/TestEnumSingletonGetterResult;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumSingletonGetterResult;->value:Ljava/lang/String;
    return-void
.end method

.method public static values()[Lenums/TestEnumSingletonGetterResult;
    .registers 1
    sget-object v0, Lenums/TestEnumSingletonGetterResult;->$VALUES:[Lenums/TestEnumSingletonGetterResult;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSingletonGetterResult;
    return-object v0
.end method
