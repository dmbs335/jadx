.class public final enum Lenums/TestEnumNestedSingleUseExpression;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumNestedSingleUseExpression;
.field public static final enum ONE:Lenums/TestEnumNestedSingleUseExpression;
.field private final value:Ljava/lang/Object;

.method static constructor <clinit>()V
    .registers 7

    const/4 v0, 0x7
    new-instance v1, Lenums/TestEnumNestedExpressionValue;
    invoke-direct {v1, v0}, Lenums/TestEnumNestedExpressionValue;-><init>(I)V
    .local v1, "value":Lenums/TestEnumNestedExpressionValue;
    filled-new-array {v1}, [Lenums/TestEnumNestedExpressionValue;
    move-result-object v2
    invoke-static {v2}, Lkotlin/collections/TestEnumNestedExpressionFactory;->listOf([Lenums/TestEnumNestedExpressionValue;)Ljava/lang/Object;
    move-result-object v3

    new-instance v4, Lenums/TestEnumNestedSingleUseExpression;
    const-string v5, "ONE"
    const/4 v6, 0x0
    invoke-direct {v4, v5, v6, v3}, Lenums/TestEnumNestedSingleUseExpression;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v4, Lenums/TestEnumNestedSingleUseExpression;->ONE:Lenums/TestEnumNestedSingleUseExpression;

    filled-new-array {v4}, [Lenums/TestEnumNestedSingleUseExpression;
    move-result-object v0
    sput-object v0, Lenums/TestEnumNestedSingleUseExpression;->$VALUES:[Lenums/TestEnumNestedSingleUseExpression;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Object;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumNestedSingleUseExpression;->value:Ljava/lang/Object;
    return-void
.end method

.method public static values()[Lenums/TestEnumNestedSingleUseExpression;
    .registers 1
    sget-object v0, Lenums/TestEnumNestedSingleUseExpression;->$VALUES:[Lenums/TestEnumNestedSingleUseExpression;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumNestedSingleUseExpression;
    return-object v0
.end method
