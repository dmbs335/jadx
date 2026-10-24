.class public final enum Lenums/TestEnumDynamicOrdinalNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumDynamicOrdinalNegative;
.field public static final enum FIRST:Lenums/TestEnumDynamicOrdinalNegative;
.field public static final enum SECOND:Lenums/TestEnumDynamicOrdinalNegative;
.field public static ORDINAL:I

.method static constructor <clinit>()V
    .registers 3
    const/4 v2, 0x7
    sput v2, Lenums/TestEnumDynamicOrdinalNegative;->ORDINAL:I
    new-instance v0, Lenums/TestEnumDynamicOrdinalNegative;
    const-string v1, "FIRST"
    sget v2, Lenums/TestEnumDynamicOrdinalNegative;->ORDINAL:I
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDynamicOrdinalNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDynamicOrdinalNegative;->FIRST:Lenums/TestEnumDynamicOrdinalNegative;
    new-instance v0, Lenums/TestEnumDynamicOrdinalNegative;
    const-string v1, "SECOND"
    const/4 v2, 0x1
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDynamicOrdinalNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDynamicOrdinalNegative;->SECOND:Lenums/TestEnumDynamicOrdinalNegative;
    sget-object v0, Lenums/TestEnumDynamicOrdinalNegative;->FIRST:Lenums/TestEnumDynamicOrdinalNegative;
    sget-object v1, Lenums/TestEnumDynamicOrdinalNegative;->SECOND:Lenums/TestEnumDynamicOrdinalNegative;
    filled-new-array {v0, v1}, [Lenums/TestEnumDynamicOrdinalNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumDynamicOrdinalNegative;->$VALUES:[Lenums/TestEnumDynamicOrdinalNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumDynamicOrdinalNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumDynamicOrdinalNegative;->$VALUES:[Lenums/TestEnumDynamicOrdinalNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumDynamicOrdinalNegative;
    return-object v0
.end method
