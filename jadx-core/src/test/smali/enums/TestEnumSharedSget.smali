.class public final enum Lenums/TestEnumSharedSget;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSharedSget;
.field public static final enum ONE:Lenums/TestEnumSharedSget;
.field public static final enum TWO:Lenums/TestEnumSharedSget;
.field public static final enum THREE:Lenums/TestEnumSharedSget;
.field private final type:Ljava/lang/Class;
.field private final value:I

.method static constructor <clinit>()V
    .registers 11

    sget-object v0, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;
    const/4 v6, 0x7
    new-instance v1, Lenums/TestEnumSharedSget;
    const-string v2, "ONE"
    const/4 v3, 0x0
    invoke-direct {v1, v2, v3, v0, v6}, Lenums/TestEnumSharedSget;-><init>(Ljava/lang/String;ILjava/lang/Class;I)V
    sput-object v1, Lenums/TestEnumSharedSget;->ONE:Lenums/TestEnumSharedSget;
    new-instance v4, Lenums/TestEnumSharedSget;
    const-string v2, "TWO"
    const/4 v5, 0x1
    invoke-direct {v4, v2, v5, v0, v6}, Lenums/TestEnumSharedSget;-><init>(Ljava/lang/String;ILjava/lang/Class;I)V
    sput-object v4, Lenums/TestEnumSharedSget;->TWO:Lenums/TestEnumSharedSget;
    invoke-virtual {v1}, Lenums/TestEnumSharedSget;->getValue()I
    move-result v9
    invoke-virtual {v4}, Lenums/TestEnumSharedSget;->getValue()I
    move-result v10
    or-int/2addr v9, v10
    new-instance v8, Lenums/TestEnumSharedSget;
    const-string v2, "THREE"
    const/4 v3, 0x2
    invoke-direct {v8, v2, v3, v0, v9}, Lenums/TestEnumSharedSget;-><init>(Ljava/lang/String;ILjava/lang/Class;I)V
    sput-object v8, Lenums/TestEnumSharedSget;->THREE:Lenums/TestEnumSharedSget;
    filled-new-array {v1, v4, v8}, [Lenums/TestEnumSharedSget;
    move-result-object v7
    sput-object v7, Lenums/TestEnumSharedSget;->$VALUES:[Lenums/TestEnumSharedSget;
    return-void
.end method

.method public final getValue()I
    .registers 2
    iget v0, p0, Lenums/TestEnumSharedSget;->value:I
    return v0
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Class;I)V
    .registers 5
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumSharedSget;->type:Ljava/lang/Class;
    iput p4, p0, Lenums/TestEnumSharedSget;->value:I
    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumSharedSget;
    .registers 2
    const-class v0, Lenums/TestEnumSharedSget;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumSharedSget;
    return-object v0
.end method

.method public static values()[Lenums/TestEnumSharedSget;
    .registers 1
    sget-object v0, Lenums/TestEnumSharedSget;->$VALUES:[Lenums/TestEnumSharedSget;
    invoke-virtual {v0}, [Lenums/TestEnumSharedSget;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSharedSget;
    return-object v0
.end method
