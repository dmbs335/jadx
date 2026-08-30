.class public final enum Lenums/TestEnumRepeatedSelectorExpression;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumRepeatedSelectorExpression;
.field public static final enum ONE:Lenums/TestEnumRepeatedSelectorExpression;
.field private final label:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 7

    sget-object v0, Lenums/TestEnumRepeatedSelectorHelper;->MAPPING:[I
    invoke-static {}, Lenums/TestEnumRepeatedSelectorHelper;->getIndex()I
    move-result v1
    aget v2, v0, v1

    const/4 v3, 0x1
    if-ne v2, v3, :not_one
    const-string v4, "one"
    goto :make

    :not_one
    const/4 v3, 0x2
    if-ne v2, v3, :not_two
    const-string v4, "two"
    goto :make

    :not_two
    const/4 v3, 0x3
    if-ne v2, v3, :default_value
    const-string v4, "three"
    goto :make

    :default_value
    const-string v4, ""

    :make
    new-instance v5, Lenums/TestEnumRepeatedSelectorExpression;
    const-string v0, "ONE"
    const/4 v1, 0x0
    invoke-direct {v5, v0, v1, v4}, Lenums/TestEnumRepeatedSelectorExpression;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    sput-object v5, Lenums/TestEnumRepeatedSelectorExpression;->ONE:Lenums/TestEnumRepeatedSelectorExpression;

    filled-new-array {v5}, [Lenums/TestEnumRepeatedSelectorExpression;
    move-result-object v6
    sput-object v6, Lenums/TestEnumRepeatedSelectorExpression;->$VALUES:[Lenums/TestEnumRepeatedSelectorExpression;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumRepeatedSelectorExpression;->label:Ljava/lang/String;
    return-void
.end method

.method public static values()[Lenums/TestEnumRepeatedSelectorExpression;
    .registers 1
    sget-object v0, Lenums/TestEnumRepeatedSelectorExpression;->$VALUES:[Lenums/TestEnumRepeatedSelectorExpression;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumRepeatedSelectorExpression;
    return-object v0
.end method
