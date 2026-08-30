.class public final enum Lenums/TestEnumNestedWrappedRegisterExpression;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumNestedWrappedRegisterExpression;
.field public static final enum ONE:Lenums/TestEnumNestedWrappedRegisterExpression;
.field public static final enum TWO:Lenums/TestEnumNestedWrappedRegisterExpression;
.field private final value:Ljava/lang/Object;

.method static constructor <clinit>()V
    .registers 10

    invoke-static {}, Lenums/NestedRegisterFactory;->base()Ljava/lang/Object;
    move-result-object v0

    filled-new-array {v0}, [Ljava/lang/Object;
    move-result-object v1
    invoke-static {v1}, Lenums/NestedRegisterFactory;->listOfArray([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2

    sget-object v3, Lenums/NestedRegisterFactory;->SHARED:Ljava/lang/Object;
    new-instance v4, Lenums/NestedRegisterValue;
    invoke-direct {v4, v2, v3, v3}, Lenums/NestedRegisterValue;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    invoke-static {v4}, Lenums/NestedRegisterFactory;->listOf(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v5

    new-instance v6, Lenums/TestEnumNestedWrappedRegisterExpression;
    const-string v7, "ONE"
    const/4 v8, 0x0
    invoke-direct {v6, v7, v8, v5}, Lenums/TestEnumNestedWrappedRegisterExpression;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v6, Lenums/TestEnumNestedWrappedRegisterExpression;->ONE:Lenums/TestEnumNestedWrappedRegisterExpression;

    invoke-static {}, Lenums/NestedRegisterFactory;->base()Ljava/lang/Object;
    move-result-object v0
    filled-new-array {v0}, [Ljava/lang/Object;
    move-result-object v1
    invoke-static {v1}, Lenums/NestedRegisterFactory;->listOfArray([Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    new-instance v4, Lenums/NestedRegisterValue;
    invoke-direct {v4, v2, v3, v3}, Lenums/NestedRegisterValue;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    invoke-static {v4}, Lenums/NestedRegisterFactory;->listOf(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v5

    new-instance v6, Lenums/TestEnumNestedWrappedRegisterExpression;
    const-string v7, "TWO"
    const/4 v8, 0x1
    invoke-direct {v6, v7, v8, v5}, Lenums/TestEnumNestedWrappedRegisterExpression;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v6, Lenums/TestEnumNestedWrappedRegisterExpression;->TWO:Lenums/TestEnumNestedWrappedRegisterExpression;

    sget-object v0, Lenums/TestEnumNestedWrappedRegisterExpression;->ONE:Lenums/TestEnumNestedWrappedRegisterExpression;
    filled-new-array {v0, v6}, [Lenums/TestEnumNestedWrappedRegisterExpression;
    move-result-object v9
    sput-object v9, Lenums/TestEnumNestedWrappedRegisterExpression;->$VALUES:[Lenums/TestEnumNestedWrappedRegisterExpression;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Object;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumNestedWrappedRegisterExpression;->value:Ljava/lang/Object;
    return-void
.end method

.method public static values()[Lenums/TestEnumNestedWrappedRegisterExpression;
    .registers 1
    sget-object v0, Lenums/TestEnumNestedWrappedRegisterExpression;->$VALUES:[Lenums/TestEnumNestedWrappedRegisterExpression;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumNestedWrappedRegisterExpression;
    return-object v0
.end method
