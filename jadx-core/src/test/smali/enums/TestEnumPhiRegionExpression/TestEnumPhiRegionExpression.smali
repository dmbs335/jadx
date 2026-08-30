.class public final enum Lenums/TestEnumPhiRegionExpression;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumPhiRegionExpression;
.field public static final enum ONE:Lenums/TestEnumPhiRegionExpression;
.field private final label:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 6

    invoke-static {}, Lenums/TestEnumPhiRegionHelper;->getPrimary()Ljava/lang/String;
    move-result-object v0
    if-nez v0, :use_primary

    invoke-static {}, Lenums/TestEnumPhiRegionHelper;->getFallback()Ljava/lang/String;
    move-result-object v1
    if-nez v1, :use_fallback
    const-string v2, ""
    goto :make

    :use_fallback
    move-object v2, v1
    goto :make

    :use_primary
    move-object v2, v0

    :make
    new-instance v3, Lenums/TestEnumPhiRegionExpression;
    const-string v4, "ONE"
    const/4 v5, 0x0
    invoke-direct {v3, v4, v5, v2}, Lenums/TestEnumPhiRegionExpression;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    sput-object v3, Lenums/TestEnumPhiRegionExpression;->ONE:Lenums/TestEnumPhiRegionExpression;

    filled-new-array {v3}, [Lenums/TestEnumPhiRegionExpression;
    move-result-object v0
    sput-object v0, Lenums/TestEnumPhiRegionExpression;->$VALUES:[Lenums/TestEnumPhiRegionExpression;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumPhiRegionExpression;->label:Ljava/lang/String;
    return-void
.end method

.method public static values()[Lenums/TestEnumPhiRegionExpression;
    .registers 1
    sget-object v0, Lenums/TestEnumPhiRegionExpression;->$VALUES:[Lenums/TestEnumPhiRegionExpression;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumPhiRegionExpression;
    return-object v0
.end method
