.class public final enum Lenums/TestEnumEncodedNameOverrideNegative;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumEncodedNameOverrideNegative;
.field public static final enum FIRST:Lenums/TestEnumEncodedNameOverrideNegative;
.field public static final NAME:Ljava/lang/String; = "FIRST"

.method static constructor <clinit>()V
    .registers 3
    const-string v1, "ACTUAL"
    sput-object v1, Lenums/TestEnumEncodedNameOverrideNegative;->NAME:Ljava/lang/String;
    new-instance v0, Lenums/TestEnumEncodedNameOverrideNegative;
    sget-object v1, Lenums/TestEnumEncodedNameOverrideNegative;->NAME:Ljava/lang/String;
    const/4 v2, 0x0
    invoke-direct {v0, v1, v2}, Lenums/TestEnumEncodedNameOverrideNegative;-><init>(Ljava/lang/String;I)V
    sput-object v0, Lenums/TestEnumEncodedNameOverrideNegative;->FIRST:Lenums/TestEnumEncodedNameOverrideNegative;
    sget-object v0, Lenums/TestEnumEncodedNameOverrideNegative;->FIRST:Lenums/TestEnumEncodedNameOverrideNegative;
    filled-new-array {v0}, [Lenums/TestEnumEncodedNameOverrideNegative;
    move-result-object v1
    sput-object v1, Lenums/TestEnumEncodedNameOverrideNegative;->$VALUES:[Lenums/TestEnumEncodedNameOverrideNegative;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .registers 3
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumEncodedNameOverrideNegative;
    .registers 1
    sget-object v0, Lenums/TestEnumEncodedNameOverrideNegative;->$VALUES:[Lenums/TestEnumEncodedNameOverrideNegative;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumEncodedNameOverrideNegative;
    return-object v0
.end method
