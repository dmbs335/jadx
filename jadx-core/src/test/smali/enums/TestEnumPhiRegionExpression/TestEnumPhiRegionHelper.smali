.class public final Lenums/TestEnumPhiRegionHelper;
.super Ljava/lang/Object;

.method public static getPrimary()Ljava/lang/String;
    .registers 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public static getFallback()Ljava/lang/String;
    .registers 1
    const-string v0, "fallback"
    return-object v0
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
