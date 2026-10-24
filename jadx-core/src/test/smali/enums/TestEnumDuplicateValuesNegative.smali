.class public final enum Lenums/TestEnumDuplicateValuesNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumDuplicateValuesNegative;
.field public static final enum ONE:Lenums/TestEnumDuplicateValuesNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumDuplicateValuesNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDuplicateValuesNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDuplicateValuesNegative;->ONE:Lenums/TestEnumDuplicateValuesNegative;
    sget-object v0, Lenums/TestEnumDuplicateValuesNegative;->ONE:Lenums/TestEnumDuplicateValuesNegative;
    filled-new-array {v0, v0}, [Lenums/TestEnumDuplicateValuesNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumDuplicateValuesNegative;->$VALUES:[Lenums/TestEnumDuplicateValuesNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumDuplicateValuesNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumDuplicateValuesNegative;->$VALUES:[Lenums/TestEnumDuplicateValuesNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumDuplicateValuesNegative;
    return-object v0
.end method
