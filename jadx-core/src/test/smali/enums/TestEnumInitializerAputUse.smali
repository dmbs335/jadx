.class public final enum Lenums/TestEnumInitializerAputUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerAputUse;
.field public static final enum ONE:Lenums/TestEnumInitializerAputUse;
.field public static seenArray:[Lenums/TestEnumInitializerAputUse;

.method static constructor <clinit>()V
    .registers 4
    new-instance v0, Lenums/TestEnumInitializerAputUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerAputUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerAputUse;->ONE:Lenums/TestEnumInitializerAputUse;
    const/4 v2, 0x1
    new-array v1, v2, [Lenums/TestEnumInitializerAputUse;
    const/4 v2, 0x0
    aput-object v0, v1, v2
    sput-object v1, Lenums/TestEnumInitializerAputUse;->seenArray:[Lenums/TestEnumInitializerAputUse;
    sget-object v1, Lenums/TestEnumInitializerAputUse;->ONE:Lenums/TestEnumInitializerAputUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerAputUse;
    move-result-object v3
    sput-object v3, Lenums/TestEnumInitializerAputUse;->$VALUES:[Lenums/TestEnumInitializerAputUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerAputUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerAputUse;->$VALUES:[Lenums/TestEnumInitializerAputUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerAputUse;
    return-object v0
.end method
