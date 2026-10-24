.class public final enum Lenums/TestEnumNameMismatchNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumNameMismatchNegative;
.field public static final enum OBF:Lenums/TestEnumNameMismatchNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumNameMismatchNegative;
    const-string v1, "ACTUAL"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumNameMismatchNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumNameMismatchNegative;->OBF:Lenums/TestEnumNameMismatchNegative;
    sget-object v0, Lenums/TestEnumNameMismatchNegative;->OBF:Lenums/TestEnumNameMismatchNegative;
    filled-new-array {v0}, [Lenums/TestEnumNameMismatchNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumNameMismatchNegative;->$VALUES:[Lenums/TestEnumNameMismatchNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumNameMismatchNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumNameMismatchNegative;->$VALUES:[Lenums/TestEnumNameMismatchNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumNameMismatchNegative;
    return-object v0
.end method
