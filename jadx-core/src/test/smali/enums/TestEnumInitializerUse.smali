.class public final enum Lenums/TestEnumInitializerUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerUse;
.field public static final enum ONE:Lenums/TestEnumInitializerUse;
.field public static seen:Lenums/TestEnumInitializerUse;

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumInitializerUse;
    .registers 2
    const-class v0, Lenums/TestEnumInitializerUse;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumInitializerUse;
    return-object v0
.end method

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumInitializerUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerUse;->ONE:Lenums/TestEnumInitializerUse;
    invoke-static {v0}, Lenums/TestEnumInitializerUse;->remember(Lenums/TestEnumInitializerUse;)V
    sget-object v1, Lenums/TestEnumInitializerUse;->ONE:Lenums/TestEnumInitializerUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerUse;
    move-result-object v2
    sput-object v2, Lenums/TestEnumInitializerUse;->$VALUES:[Lenums/TestEnumInitializerUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method private static remember(Lenums/TestEnumInitializerUse;)V
    .registers 1
    sput-object p0, Lenums/TestEnumInitializerUse;->seen:Lenums/TestEnumInitializerUse;
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerUse;->$VALUES:[Lenums/TestEnumInitializerUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerUse;
    return-object v0
.end method
