.class public final enum Lenums/TestEnumInitializerFieldUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerFieldUse;
.field public static final enum ONE:Lenums/TestEnumInitializerFieldUse;
.field public static seen:Lenums/TestEnumInitializerFieldUse;

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumInitializerFieldUse;
    .registers 2
    const-class v0, Lenums/TestEnumInitializerFieldUse;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumInitializerFieldUse;
    return-object v0
.end method

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumInitializerFieldUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerFieldUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerFieldUse;->ONE:Lenums/TestEnumInitializerFieldUse;
    sput-object v0, Lenums/TestEnumInitializerFieldUse;->seen:Lenums/TestEnumInitializerFieldUse;
    sget-object v1, Lenums/TestEnumInitializerFieldUse;->ONE:Lenums/TestEnumInitializerFieldUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerFieldUse;
    move-result-object v2
    sput-object v2, Lenums/TestEnumInitializerFieldUse;->$VALUES:[Lenums/TestEnumInitializerFieldUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerFieldUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerFieldUse;->$VALUES:[Lenums/TestEnumInitializerFieldUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerFieldUse;
    return-object v0
.end method
