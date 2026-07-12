.class public final enum Lenums/TestEnumSingleUseGetter;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSingleUseGetter;
.field public static final enum ONE:Lenums/TestEnumSingleUseGetter;
.field public static final enum TWO:Lenums/TestEnumSingleUseGetter;
.field private final label:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 7
    sget-object v0, Lenums/TestEnumSingleUseGetter$Helper;->INSTANCE:Lenums/TestEnumSingleUseGetter$Helper;
    invoke-virtual {v0}, Lenums/TestEnumSingleUseGetter$Helper;->getType()Lenums/TestEnumSingleUseGetter$Mode;
    move-result-object v1
    sget-object v2, Lenums/TestEnumSingleUseGetter$Mode;->TYPE24:Lenums/TestEnumSingleUseGetter$Mode;
    if-ne v1, v2, :one_alt
    const-string v3, "A"
    goto :one_make
    :one_alt
    const-string v3, "B"
    :one_make
    new-instance v4, Lenums/TestEnumSingleUseGetter;
    const-string v5, "ONE"
    const/4 v6, 0x0
    invoke-direct {v4, v5, v6, v3}, Lenums/TestEnumSingleUseGetter;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    sput-object v4, Lenums/TestEnumSingleUseGetter;->ONE:Lenums/TestEnumSingleUseGetter;

    invoke-virtual {v0}, Lenums/TestEnumSingleUseGetter$Helper;->getType()Lenums/TestEnumSingleUseGetter$Mode;
    move-result-object v1
    if-ne v1, v2, :two_alt
    const-string v3, "C"
    goto :two_make
    :two_alt
    const-string v3, "D"
    :two_make
    new-instance v4, Lenums/TestEnumSingleUseGetter;
    const-string v5, "TWO"
    const/4 v6, 0x1
    invoke-direct {v4, v5, v6, v3}, Lenums/TestEnumSingleUseGetter;-><init>(Ljava/lang/String;ILjava/lang/String;)V
    sput-object v4, Lenums/TestEnumSingleUseGetter;->TWO:Lenums/TestEnumSingleUseGetter;

    sget-object v0, Lenums/TestEnumSingleUseGetter;->ONE:Lenums/TestEnumSingleUseGetter;
    sget-object v1, Lenums/TestEnumSingleUseGetter;->TWO:Lenums/TestEnumSingleUseGetter;
    filled-new-array {v0, v1}, [Lenums/TestEnumSingleUseGetter;
    move-result-object v0
    sput-object v0, Lenums/TestEnumSingleUseGetter;->$VALUES:[Lenums/TestEnumSingleUseGetter;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/String;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumSingleUseGetter;->label:Ljava/lang/String;
    return-void
.end method

.method public static values()[Lenums/TestEnumSingleUseGetter;
    .registers 1
    sget-object v0, Lenums/TestEnumSingleUseGetter;->$VALUES:[Lenums/TestEnumSingleUseGetter;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSingleUseGetter;
    return-object v0
.end method
