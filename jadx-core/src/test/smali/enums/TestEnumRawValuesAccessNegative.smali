.class public final enum Lenums/TestEnumRawValuesAccessNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumRawValuesAccessNegative;
.field public static final enum ONE:Lenums/TestEnumRawValuesAccessNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumRawValuesAccessNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumRawValuesAccessNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumRawValuesAccessNegative;->ONE:Lenums/TestEnumRawValuesAccessNegative;
    sget-object v0, Lenums/TestEnumRawValuesAccessNegative;->ONE:Lenums/TestEnumRawValuesAccessNegative;
    filled-new-array {v0}, [Lenums/TestEnumRawValuesAccessNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumRawValuesAccessNegative;->$VALUES:[Lenums/TestEnumRawValuesAccessNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumRawValuesAccessNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumRawValuesAccessNegative;->$VALUES:[Lenums/TestEnumRawValuesAccessNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumRawValuesAccessNegative;
    return-object v0
.end method

.method public static sameRawArray()Z
    .registers 2
    sget-object v0, Lenums/TestEnumRawValuesAccessNegative;->$VALUES:[Lenums/TestEnumRawValuesAccessNegative;
    sget-object v1, Lenums/TestEnumRawValuesAccessNegative;->$VALUES:[Lenums/TestEnumRawValuesAccessNegative;
    if-ne v0, v1, :different
    const/4 v0, 0x1
    return v0
    :different
    const/4 v0, 0x0
    return v0
.end method
