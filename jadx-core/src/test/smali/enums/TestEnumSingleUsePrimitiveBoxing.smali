.class public final enum Lenums/TestEnumSingleUsePrimitiveBoxing;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSingleUsePrimitiveBoxing;
.field public static final enum ONE:Lenums/TestEnumSingleUsePrimitiveBoxing;
.field private final value:Ljava/lang/Integer;

.method static constructor <clinit>()V
    .registers 5

    const/4 v0, 0x7
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v1
    new-instance v2, Lenums/TestEnumSingleUsePrimitiveBoxing;
    const-string v3, "ONE"
    const/4 v4, 0x0
    invoke-direct {v2, v3, v4, v1}, Lenums/TestEnumSingleUsePrimitiveBoxing;-><init>(Ljava/lang/String;ILjava/lang/Integer;)V
    sput-object v2, Lenums/TestEnumSingleUsePrimitiveBoxing;->ONE:Lenums/TestEnumSingleUsePrimitiveBoxing;

    sget-object v0, Lenums/TestEnumSingleUsePrimitiveBoxing;->ONE:Lenums/TestEnumSingleUsePrimitiveBoxing;
    filled-new-array {v0}, [Lenums/TestEnumSingleUsePrimitiveBoxing;
    move-result-object v0
    sput-object v0, Lenums/TestEnumSingleUsePrimitiveBoxing;->$VALUES:[Lenums/TestEnumSingleUsePrimitiveBoxing;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Integer;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumSingleUsePrimitiveBoxing;->value:Ljava/lang/Integer;
    return-void
.end method

.method public static values()[Lenums/TestEnumSingleUsePrimitiveBoxing;
    .registers 1
    sget-object v0, Lenums/TestEnumSingleUsePrimitiveBoxing;->$VALUES:[Lenums/TestEnumSingleUsePrimitiveBoxing;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSingleUsePrimitiveBoxing;
    return-object v0
.end method
