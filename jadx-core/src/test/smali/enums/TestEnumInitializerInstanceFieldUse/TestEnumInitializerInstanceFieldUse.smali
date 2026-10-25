.class public final enum Lenums/TestEnumInitializerInstanceFieldUse;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumInitializerInstanceFieldUse;
.field public static final enum ONE:Lenums/TestEnumInitializerInstanceFieldUse;
.field public static holder:Lenums/TestEnumInitializerInstanceFieldUse$Holder;

.method public static valueOf(Ljava/lang/String;)Lenums/TestEnumInitializerInstanceFieldUse;
    .registers 2
    const-class v0, Lenums/TestEnumInitializerInstanceFieldUse;
    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;
    move-result-object v0
    check-cast v0, Lenums/TestEnumInitializerInstanceFieldUse;
    return-object v0
.end method

.method static constructor <clinit>()V
    .registers 4
    new-instance v0, Lenums/TestEnumInitializerInstanceFieldUse;
    const-string v1, "ONE"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumInitializerInstanceFieldUse;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumInitializerInstanceFieldUse;->ONE:Lenums/TestEnumInitializerInstanceFieldUse;
    new-instance v1, Lenums/TestEnumInitializerInstanceFieldUse$Holder;
    invoke-direct {v1}, Lenums/TestEnumInitializerInstanceFieldUse$Holder;-><init>()V
    sput-object v1, Lenums/TestEnumInitializerInstanceFieldUse;->holder:Lenums/TestEnumInitializerInstanceFieldUse$Holder;
    iput-object v0, v1, Lenums/TestEnumInitializerInstanceFieldUse$Holder;->seen:Lenums/TestEnumInitializerInstanceFieldUse;
    sget-object v1, Lenums/TestEnumInitializerInstanceFieldUse;->ONE:Lenums/TestEnumInitializerInstanceFieldUse;
    filled-new-array {v1}, [Lenums/TestEnumInitializerInstanceFieldUse;
    move-result-object v3
    sput-object v3, Lenums/TestEnumInitializerInstanceFieldUse;->$VALUES:[Lenums/TestEnumInitializerInstanceFieldUse;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumInitializerInstanceFieldUse;
    .registers 1
    sget-object v0, Lenums/TestEnumInitializerInstanceFieldUse;->$VALUES:[Lenums/TestEnumInitializerInstanceFieldUse;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumInitializerInstanceFieldUse;
    return-object v0
.end method
