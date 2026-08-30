.class public final Lenums/TestEnumRepeatedSelectorHelper;
.super Ljava/lang/Object;

.field public static final MAPPING:[I

.method static constructor <clinit>()V
    .registers 3
    const/4 v0, 0x4
    new-array v0, v0, [I
    const/4 v1, 0x1
    aput v1, v0, v1
    const/4 v1, 0x2
    aput v1, v0, v1
    const/4 v1, 0x3
    aput v1, v0, v1
    sput-object v0, Lenums/TestEnumRepeatedSelectorHelper;->MAPPING:[I
    return-void
.end method

.method public static getIndex()I
    .registers 1
    const/4 v0, 0x2
    return v0
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
