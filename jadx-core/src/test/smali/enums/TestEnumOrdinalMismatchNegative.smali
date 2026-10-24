.class public final enum Lenums/TestEnumOrdinalMismatchNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumOrdinalMismatchNegative;
.field public static final enum FIRST:Lenums/TestEnumOrdinalMismatchNegative;
.field public static final enum SECOND:Lenums/TestEnumOrdinalMismatchNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumOrdinalMismatchNegative;
    const-string v1, "FIRST"
    const/4 v2, 0x7
    invoke-direct {v0, v1, v2}, Lenums/TestEnumOrdinalMismatchNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumOrdinalMismatchNegative;->FIRST:Lenums/TestEnumOrdinalMismatchNegative;
    new-instance v0, Lenums/TestEnumOrdinalMismatchNegative;
    const-string v1, "SECOND"
    const/16 v2, 0x8
    invoke-direct {v0, v1, v2}, Lenums/TestEnumOrdinalMismatchNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumOrdinalMismatchNegative;->SECOND:Lenums/TestEnumOrdinalMismatchNegative;
    sget-object v0, Lenums/TestEnumOrdinalMismatchNegative;->FIRST:Lenums/TestEnumOrdinalMismatchNegative;
    sget-object v1, Lenums/TestEnumOrdinalMismatchNegative;->SECOND:Lenums/TestEnumOrdinalMismatchNegative;
    filled-new-array {v0, v1}, [Lenums/TestEnumOrdinalMismatchNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumOrdinalMismatchNegative;->$VALUES:[Lenums/TestEnumOrdinalMismatchNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumOrdinalMismatchNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumOrdinalMismatchNegative;->$VALUES:[Lenums/TestEnumOrdinalMismatchNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumOrdinalMismatchNegative;
    return-object v0
.end method
