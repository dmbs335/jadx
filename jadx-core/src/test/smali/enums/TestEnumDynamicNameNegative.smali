.class public final enum Lenums/TestEnumDynamicNameNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumDynamicNameNegative;
.field public static final enum FIRST:Lenums/TestEnumDynamicNameNegative;
.field public static NAME:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 3
    const-string v1, "ACTUAL"
    sput-object v1, Lenums/TestEnumDynamicNameNegative;->NAME:Ljava/lang/String;
    new-instance v0, Lenums/TestEnumDynamicNameNegative;
    sget-object v1, Lenums/TestEnumDynamicNameNegative;->NAME:Ljava/lang/String;
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDynamicNameNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDynamicNameNegative;->FIRST:Lenums/TestEnumDynamicNameNegative;
    sget-object v0, Lenums/TestEnumDynamicNameNegative;->FIRST:Lenums/TestEnumDynamicNameNegative;
    filled-new-array {v0}, [Lenums/TestEnumDynamicNameNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumDynamicNameNegative;->$VALUES:[Lenums/TestEnumDynamicNameNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumDynamicNameNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumDynamicNameNegative;->$VALUES:[Lenums/TestEnumDynamicNameNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumDynamicNameNegative;
    return-object v0
.end method
