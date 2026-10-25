.class public final enum Lenums/TestEnumMissingValueOfNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumMissingValueOfNegative;
.field public static final enum ONE:Lenums/TestEnumMissingValueOfNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumMissingValueOfNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumMissingValueOfNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumMissingValueOfNegative;->ONE:Lenums/TestEnumMissingValueOfNegative;
    sget-object v0, Lenums/TestEnumMissingValueOfNegative;->ONE:Lenums/TestEnumMissingValueOfNegative;
    filled-new-array {v0}, [Lenums/TestEnumMissingValueOfNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumMissingValueOfNegative;->$VALUES:[Lenums/TestEnumMissingValueOfNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumMissingValueOfNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumMissingValueOfNegative;->$VALUES:[Lenums/TestEnumMissingValueOfNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumMissingValueOfNegative;
    return-object v0
.end method
