.class public final enum Lenums/TestEnumValuesOverwriteNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumValuesOverwriteNegative;
.field public static final enum ONE:Lenums/TestEnumValuesOverwriteNegative;
.field public static final enum TWO:Lenums/TestEnumValuesOverwriteNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumValuesOverwriteNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumValuesOverwriteNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumValuesOverwriteNegative;->ONE:Lenums/TestEnumValuesOverwriteNegative;
    new-instance v0, Lenums/TestEnumValuesOverwriteNegative;
    const-string v1, "TWO"
    const/4 v2, 0x1
    invoke-direct {v0, v1, v2}, Lenums/TestEnumValuesOverwriteNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumValuesOverwriteNegative;->TWO:Lenums/TestEnumValuesOverwriteNegative;
    sget-object v0, Lenums/TestEnumValuesOverwriteNegative;->ONE:Lenums/TestEnumValuesOverwriteNegative;
    sget-object v1, Lenums/TestEnumValuesOverwriteNegative;->TWO:Lenums/TestEnumValuesOverwriteNegative;
    filled-new-array {v0, v1}, [Lenums/TestEnumValuesOverwriteNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumValuesOverwriteNegative;->$VALUES:[Lenums/TestEnumValuesOverwriteNegative;
    filled-new-array {v0}, [Lenums/TestEnumValuesOverwriteNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumValuesOverwriteNegative;->$VALUES:[Lenums/TestEnumValuesOverwriteNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumValuesOverwriteNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumValuesOverwriteNegative;->$VALUES:[Lenums/TestEnumValuesOverwriteNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumValuesOverwriteNegative;
    return-object v0
.end method
