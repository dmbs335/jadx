.class public final enum Lenums/TestEnumMissingValuesNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumMissingValuesNegative;
.field public static final enum ONE:Lenums/TestEnumMissingValuesNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumMissingValuesNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumMissingValuesNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumMissingValuesNegative;->ONE:Lenums/TestEnumMissingValuesNegative;
    sget-object v0, Lenums/TestEnumMissingValuesNegative;->ONE:Lenums/TestEnumMissingValuesNegative;
    filled-new-array {v0}, [Lenums/TestEnumMissingValuesNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumMissingValuesNegative;->$VALUES:[Lenums/TestEnumMissingValuesNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumMissingValuesNegative;
    .registers 2
    const-class v0, Lenums/TestEnumMissingValuesNegative;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumMissingValuesNegative;
    return-object v0
.end method
