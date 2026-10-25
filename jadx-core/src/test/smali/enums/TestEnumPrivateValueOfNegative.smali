.class public final enum Lenums/TestEnumPrivateValueOfNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumPrivateValueOfNegative;
.field public static final enum ONE:Lenums/TestEnumPrivateValueOfNegative;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumPrivateValueOfNegative;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumPrivateValueOfNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumPrivateValueOfNegative;->ONE:Lenums/TestEnumPrivateValueOfNegative;
    sget-object v0, Lenums/TestEnumPrivateValueOfNegative;->ONE:Lenums/TestEnumPrivateValueOfNegative;
    filled-new-array {v0}, [Lenums/TestEnumPrivateValueOfNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumPrivateValueOfNegative;->$VALUES:[Lenums/TestEnumPrivateValueOfNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumPrivateValueOfNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumPrivateValueOfNegative;->$VALUES:[Lenums/TestEnumPrivateValueOfNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumPrivateValueOfNegative;
    return-object v0
.end method

.method private static valueOf(Ljava/lang/String;)Lenums/TestEnumPrivateValueOfNegative;
    .registers 2
    const-class v0, Lenums/TestEnumPrivateValueOfNegative;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumPrivateValueOfNegative;
    return-object v0
.end method
