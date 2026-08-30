.class public final Lenums/TestEnumSingleUseGetter$Mode;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lenums/TestEnumSingleUseGetter;
.end annotation
.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "Mode"
.end annotation

.field public static final TYPE24:Lenums/TestEnumSingleUseGetter$Mode;

.method static constructor <clinit>()V
    .registers 1
    new-instance v0, Lenums/TestEnumSingleUseGetter$Mode;
    invoke-direct {v0}, Lenums/TestEnumSingleUseGetter$Mode;-><init>()V
    sput-object v0, Lenums/TestEnumSingleUseGetter$Mode;->TYPE24:Lenums/TestEnumSingleUseGetter$Mode;
    return-void
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
