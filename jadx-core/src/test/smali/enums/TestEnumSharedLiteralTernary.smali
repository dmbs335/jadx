.class public final enum Lenums/TestEnumSharedLiteralTernary;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSharedLiteralTernary;
.field public static final enum A:Lenums/TestEnumSharedLiteralTernary;
.field public static final enum B:Lenums/TestEnumSharedLiteralTernary;
.field private final value:Ljava/lang/Object;

.method static constructor <clinit>()V
    .registers 8

    const/4 v0, 0x0
    if-eqz v0, :zero
    const/4 v1, 0x1
    goto :ternary_end
    :zero
    const/4 v1, 0x0
    :ternary_end

    new-instance v2, Lenums/TestEnumSharedLiteralTernary;
    const-string v3, "A"
    const/4 v4, 0x0
    invoke-direct {v2, v3, v4, v1}, Lenums/TestEnumSharedLiteralTernary;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v2, Lenums/TestEnumSharedLiteralTernary;->A:Lenums/TestEnumSharedLiteralTernary;

    new-instance v5, Lenums/TestEnumSharedLiteralTernary;
    const-string v6, "B"
    const/4 v7, 0x1
    invoke-direct {v5, v6, v7, v1}, Lenums/TestEnumSharedLiteralTernary;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v5, Lenums/TestEnumSharedLiteralTernary;->B:Lenums/TestEnumSharedLiteralTernary;

    filled-new-array {v2, v5}, [Lenums/TestEnumSharedLiteralTernary;
    move-result-object v0
    sput-object v0, Lenums/TestEnumSharedLiteralTernary;->$VALUES:[Lenums/TestEnumSharedLiteralTernary;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Object;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumSharedLiteralTernary;->value:Ljava/lang/Object;
    return-void
.end method

.method public static values()[Lenums/TestEnumSharedLiteralTernary;
    .registers 1
    sget-object v0, Lenums/TestEnumSharedLiteralTernary;->$VALUES:[Lenums/TestEnumSharedLiteralTernary;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSharedLiteralTernary;
    return-object v0
.end method
