.class public final enum Lenums/TestEnumCustomValueOfNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumCustomValueOfNegative;
.field public static final enum ONE:Lenums/TestEnumCustomValueOfNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumCustomValueOfNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumCustomValueOfNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumCustomValueOfNegative;->ONE:Lenums/TestEnumCustomValueOfNegative;
    sget-object v0, Lenums/TestEnumCustomValueOfNegative;->ONE:Lenums/TestEnumCustomValueOfNegative;
    filled-new-array {v0}, [Lenums/TestEnumCustomValueOfNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumCustomValueOfNegative;->$VALUES:[Lenums/TestEnumCustomValueOfNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumCustomValueOfNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumCustomValueOfNegative;->$VALUES:[Lenums/TestEnumCustomValueOfNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumCustomValueOfNegative;
    return-object v0
.end method

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumCustomValueOfNegative;
    .registers 2
    const-class v0, Lenums/TestEnumCustomValueOfNegative;
    const-string v1, "ONE"
    invoke-static {v0, v1}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumCustomValueOfNegative;
    return-object v0
.end method
