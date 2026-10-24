.class public final enum Lenums/TestEnumDuplicateNamesNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumDuplicateNamesNegative;
.field public static final enum FIRST:Lenums/TestEnumDuplicateNamesNegative;
.field public static final enum SECOND:Lenums/TestEnumDuplicateNamesNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumDuplicateNamesNegative;
    const-string v1, "SAME"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDuplicateNamesNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDuplicateNamesNegative;->FIRST:Lenums/TestEnumDuplicateNamesNegative;
    new-instance v0, Lenums/TestEnumDuplicateNamesNegative;
    const-string v1, "SAME"
    const/4 v2, 0x1
    invoke-direct {v0, v1, v2}, Lenums/TestEnumDuplicateNamesNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumDuplicateNamesNegative;->SECOND:Lenums/TestEnumDuplicateNamesNegative;
    sget-object v0, Lenums/TestEnumDuplicateNamesNegative;->FIRST:Lenums/TestEnumDuplicateNamesNegative;
    sget-object v1, Lenums/TestEnumDuplicateNamesNegative;->SECOND:Lenums/TestEnumDuplicateNamesNegative;
    filled-new-array {v0, v1}, [Lenums/TestEnumDuplicateNamesNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumDuplicateNamesNegative;->$VALUES:[Lenums/TestEnumDuplicateNamesNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumDuplicateNamesNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumDuplicateNamesNegative;->$VALUES:[Lenums/TestEnumDuplicateNamesNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumDuplicateNamesNegative;
    return-object v0
.end method
