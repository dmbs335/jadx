.class public final enum Lenums/TestEnumExternalValueNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumExternalValueNegative;
.field public static final enum ONE:Lenums/TestEnumExternalValueNegative;
.field public static final enum TWO:Lenums/TestEnumExternalValueNegative;
.field private final type:Ljava/lang/Class;
.field private final timestamp:J

.method static constructor <clinit>()V
    .registers 7
    sget-object v3, Ljava/lang/Integer;->TYPE:Ljava/lang/Class;
    invoke-static {}, Ljava/lang/System;->nanoTime()J
    move-result-wide v4
    new-instance v0, Lenums/TestEnumExternalValueNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct/range {v0 .. v5}, Lenums/TestEnumExternalValueNegative;-><init>(Ljava/lang/String;ILjava/lang/Class;J)V
    sput-object v0, Lenums/TestEnumExternalValueNegative;->ONE:Lenums/TestEnumExternalValueNegative;
    new-instance v0, Lenums/TestEnumExternalValueNegative;
    const-string v1, "TWO"
    const/4 v2, 0x1
    invoke-direct/range {v0 .. v5}, Lenums/TestEnumExternalValueNegative;-><init>(Ljava/lang/String;ILjava/lang/Class;J)V
    sput-object v0, Lenums/TestEnumExternalValueNegative;->TWO:Lenums/TestEnumExternalValueNegative;
    sget-object v1, Lenums/TestEnumExternalValueNegative;->ONE:Lenums/TestEnumExternalValueNegative;
    sget-object v2, Lenums/TestEnumExternalValueNegative;->TWO:Lenums/TestEnumExternalValueNegative;
    filled-new-array {v1, v2}, [Lenums/TestEnumExternalValueNegative;
    move-result-object v6
    sput-object v6, Lenums/TestEnumExternalValueNegative;->$VALUES:[Lenums/TestEnumExternalValueNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILjava/lang/Class;J)V
    .registers 7
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumExternalValueNegative;->type:Ljava/lang/Class;
    iput-wide p4, p0, Lenums/TestEnumExternalValueNegative;->timestamp:J
    return-void
.end method

.method public static values()[Lenums/TestEnumExternalValueNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumExternalValueNegative;->$VALUES:[Lenums/TestEnumExternalValueNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumExternalValueNegative;
    return-object v0
.end method
