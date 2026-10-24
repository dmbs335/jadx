.class public final enum Lenums/TestEnumInitializerConstructorUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerConstructorUse;
.field public static final enum ONE:Lenums/TestEnumInitializerConstructorUse;
.field public static holder:Lenums/TestEnumInitializerConstructorUse$Holder;

.method static constructor <clinit>()V
    .registers 4
    new-instance v0, Lenums/TestEnumInitializerConstructorUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerConstructorUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerConstructorUse;->ONE:Lenums/TestEnumInitializerConstructorUse;
    new-instance v1, Lenums/TestEnumInitializerConstructorUse$Holder;
    invoke-direct {v1, v0}, Lenums/TestEnumInitializerConstructorUse$Holder;-><init>(Lenums/TestEnumInitializerConstructorUse;)V
    sput-object v1, Lenums/TestEnumInitializerConstructorUse;->holder:Lenums/TestEnumInitializerConstructorUse$Holder;
    sget-object v1, Lenums/TestEnumInitializerConstructorUse;->ONE:Lenums/TestEnumInitializerConstructorUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerConstructorUse;
    move-result-object v3
    sput-object v3, Lenums/TestEnumInitializerConstructorUse;->$VALUES:[Lenums/TestEnumInitializerConstructorUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerConstructorUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerConstructorUse;->$VALUES:[Lenums/TestEnumInitializerConstructorUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerConstructorUse;
    return-object v0
.end method
