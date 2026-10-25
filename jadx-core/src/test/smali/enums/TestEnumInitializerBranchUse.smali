.class public final enum Lenums/TestEnumInitializerBranchUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerBranchUse;
.field public static final enum ONE:Lenums/TestEnumInitializerBranchUse;
.field public static seen:Z

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumInitializerBranchUse;
    .registers 2
    const-class v0, Lenums/TestEnumInitializerBranchUse;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumInitializerBranchUse;
    return-object v0
.end method

.method static constructor <clinit>()V
    .registers 3
    new-instance v0, Lenums/TestEnumInitializerBranchUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerBranchUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerBranchUse;->ONE:Lenums/TestEnumInitializerBranchUse;
    if-eqz v0, :after_seen
    const/4 v1, 0x1
    sput-boolean v1, Lenums/TestEnumInitializerBranchUse;->seen:Z
    :after_seen
    sget-object v1, Lenums/TestEnumInitializerBranchUse;->ONE:Lenums/TestEnumInitializerBranchUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerBranchUse;
    move-result-object v2
    sput-object v2, Lenums/TestEnumInitializerBranchUse;->$VALUES:[Lenums/TestEnumInitializerBranchUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerBranchUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerBranchUse;->$VALUES:[Lenums/TestEnumInitializerBranchUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerBranchUse;
    return-object v0
.end method
