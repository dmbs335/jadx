.class public final enum Lenums/TestEnumConstantOverwriteNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumConstantOverwriteNegative;
.field public static final enum ONE:Lenums/TestEnumConstantOverwriteNegative;
.field public static final enum TWO:Lenums/TestEnumConstantOverwriteNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumConstantOverwriteNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumConstantOverwriteNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumConstantOverwriteNegative;->ONE:Lenums/TestEnumConstantOverwriteNegative;
    new-instance v0, Lenums/TestEnumConstantOverwriteNegative;
    const-string v1, "TWO"
    const/4 v2, 0x1
    invoke-direct {v0, v1, v2}, Lenums/TestEnumConstantOverwriteNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumConstantOverwriteNegative;->TWO:Lenums/TestEnumConstantOverwriteNegative;
    sget-object v0, Lenums/TestEnumConstantOverwriteNegative;->ONE:Lenums/TestEnumConstantOverwriteNegative;
    sget-object v1, Lenums/TestEnumConstantOverwriteNegative;->TWO:Lenums/TestEnumConstantOverwriteNegative;
    filled-new-array {v0, v1}, [Lenums/TestEnumConstantOverwriteNegative;
    move-result-object v2
    sput-object v2, Lenums/TestEnumConstantOverwriteNegative;->$VALUES:[Lenums/TestEnumConstantOverwriteNegative;
    sput-object v1, Lenums/TestEnumConstantOverwriteNegative;->ONE:Lenums/TestEnumConstantOverwriteNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumConstantOverwriteNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumConstantOverwriteNegative;->$VALUES:[Lenums/TestEnumConstantOverwriteNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumConstantOverwriteNegative;
    return-object v0
.end method
