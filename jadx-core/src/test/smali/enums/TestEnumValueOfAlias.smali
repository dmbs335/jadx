.class public final enum Lenums/TestEnumValueOfAlias;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumValueOfAlias;
.field public static final enum ONE:Lenums/TestEnumValueOfAlias;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumValueOfAlias;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumValueOfAlias;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumValueOfAlias;->ONE:Lenums/TestEnumValueOfAlias;
    sget-object v0, Lenums/TestEnumValueOfAlias;->ONE:Lenums/TestEnumValueOfAlias;
    filled-new-array {v0}, [Lenums/TestEnumValueOfAlias;
    move-result-object v1
    sput-object v1, Lenums/TestEnumValueOfAlias;->$VALUES:[Lenums/TestEnumValueOfAlias;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumValueOfAlias;
    .registers 1
    sget-object v0, Lenums/TestEnumValueOfAlias;->$VALUES:[Lenums/TestEnumValueOfAlias;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumValueOfAlias;
    return-object v0
.end method

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumValueOfAlias;
    .registers 2
    const-class v0, Lenums/TestEnumValueOfAlias;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumValueOfAlias;
    return-object v0
.end method

.method public static lookup(Ljava/lang/String;)Lenums/TestEnumValueOfAlias;
    .registers 2
    const-class v0, Lenums/TestEnumValueOfAlias;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumValueOfAlias;
    return-object v0
.end method
