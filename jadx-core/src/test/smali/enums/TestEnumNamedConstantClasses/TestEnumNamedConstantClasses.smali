.class public abstract enum Lenums/TestEnumNamedConstantClasses;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumNamedConstantClasses;
.field public static final enum ALPHA:Lenums/TestEnumNamedConstantClasses;
.field public static final enum BETA:Lenums/TestEnumNamedConstantClasses;

.method static constructor <clinit>()V
    .registers 5

    const/4 v4, 0x0
    new-instance v0, Lenums/TestEnumNamedConstantClasses$ALPHA;
    const-string v1, "ALPHA"
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2, v4}, Lenums/TestEnumNamedConstantClasses$ALPHA;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v0, Lenums/TestEnumNamedConstantClasses;->ALPHA:Lenums/TestEnumNamedConstantClasses;

    new-instance v1, Lenums/TestEnumNamedConstantClasses$BETA;
    const-string v2, "BETA"
    const/4 v3, 0x1
    invoke-direct {v1, v2, v3, v4}, Lenums/TestEnumNamedConstantClasses$BETA;-><init>(Ljava/lang/String;ILjava/lang/Object;)V
    sput-object v1, Lenums/TestEnumNamedConstantClasses;->BETA:Lenums/TestEnumNamedConstantClasses;

    filled-new-array {v0, v1}, [Lenums/TestEnumNamedConstantClasses;
    move-result-object v0
    sput-object v0, Lenums/TestEnumNamedConstantClasses;->$VALUES:[Lenums/TestEnumNamedConstantClasses;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumNamedConstantClasses;
    .registers 1
    sget-object v0, Lenums/TestEnumNamedConstantClasses;->$VALUES:[Lenums/TestEnumNamedConstantClasses;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumNamedConstantClasses;
    return-object v0
.end method
