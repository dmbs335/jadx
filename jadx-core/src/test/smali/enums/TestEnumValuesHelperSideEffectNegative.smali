.class public final enum Lenums/TestEnumValuesHelperSideEffectNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumValuesHelperSideEffectNegative;
.field public static final enum ONE:Lenums/TestEnumValuesHelperSideEffectNegative;
.field public static SIDE_EFFECT:Z

.method private static synthetic $values()[Lenums/TestEnumValuesHelperSideEffectNegative;
    .registers 2
    const/4 v1, 0x1
    sput-boolean v1, Lenums/TestEnumValuesHelperSideEffectNegative;->SIDE_EFFECT:Z
    sget-object v0, Lenums/TestEnumValuesHelperSideEffectNegative;->ONE:Lenums/TestEnumValuesHelperSideEffectNegative;
    filled-new-array {v0}, [Lenums/TestEnumValuesHelperSideEffectNegative;
    move-result-object v0
    return-object v0
.end method

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumValuesHelperSideEffectNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumValuesHelperSideEffectNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumValuesHelperSideEffectNegative;->ONE:Lenums/TestEnumValuesHelperSideEffectNegative;
    invoke-static {}, Lenums/TestEnumValuesHelperSideEffectNegative;->$values()[Lenums/TestEnumValuesHelperSideEffectNegative;
    move-result-object v0
    sput-object v0, Lenums/TestEnumValuesHelperSideEffectNegative;->$VALUES:[Lenums/TestEnumValuesHelperSideEffectNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumValuesHelperSideEffectNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumValuesHelperSideEffectNegative;->$VALUES:[Lenums/TestEnumValuesHelperSideEffectNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumValuesHelperSideEffectNegative;
    return-object v0
.end method
