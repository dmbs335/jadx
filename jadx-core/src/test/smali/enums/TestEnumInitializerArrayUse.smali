.class public final enum Lenums/TestEnumInitializerArrayUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerArrayUse;
.field public static final enum ONE:Lenums/TestEnumInitializerArrayUse;
.field public static seenArray:[Lenums/TestEnumInitializerArrayUse;

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumInitializerArrayUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerArrayUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerArrayUse;->ONE:Lenums/TestEnumInitializerArrayUse;
    filled-new-array {v0}, [Lenums/TestEnumInitializerArrayUse;
    move-result-object v1
    sput-object v1, Lenums/TestEnumInitializerArrayUse;->seenArray:[Lenums/TestEnumInitializerArrayUse;
    sget-object v1, Lenums/TestEnumInitializerArrayUse;->ONE:Lenums/TestEnumInitializerArrayUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerArrayUse;
    move-result-object v2
    sput-object v2, Lenums/TestEnumInitializerArrayUse;->$VALUES:[Lenums/TestEnumInitializerArrayUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerArrayUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerArrayUse;->$VALUES:[Lenums/TestEnumInitializerArrayUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerArrayUse;
    return-object v0
.end method
