.class public final Lenums/TestEnumSingleUseGetter$Helper;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lenums/TestEnumSingleUseGetter;
.end annotation
.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "Helper"
.end annotation

.field public static final INSTANCE:Lenums/TestEnumSingleUseGetter$Helper;
.field private final type:Lenums/TestEnumSingleUseGetter$Mode;

.method static constructor <clinit>()V
    .registers 1
    new-instance v0, Lenums/TestEnumSingleUseGetter$Helper;
    invoke-direct {v0}, Lenums/TestEnumSingleUseGetter$Helper;-><init>()V
    sput-object v0, Lenums/TestEnumSingleUseGetter$Helper;->INSTANCE:Lenums/TestEnumSingleUseGetter$Helper;
    return-void
.end method

.method private constructor <init>()V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    sget-object v0, Lenums/TestEnumSingleUseGetter$Mode;->TYPE24:Lenums/TestEnumSingleUseGetter$Mode;
    iput-object v0, p0, Lenums/TestEnumSingleUseGetter$Helper;->type:Lenums/TestEnumSingleUseGetter$Mode;
    return-void
.end method

.method public final getType()Lenums/TestEnumSingleUseGetter$Mode;
    .registers 2
    iget-object v0, p0, Lenums/TestEnumSingleUseGetter$Helper;->type:Lenums/TestEnumSingleUseGetter$Mode;
    return-object v0
.end method
