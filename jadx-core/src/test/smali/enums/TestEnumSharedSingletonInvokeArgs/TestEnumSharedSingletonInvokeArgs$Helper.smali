.class public final Lenums/TestEnumSharedSingletonInvokeArgs$Helper;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lenums/TestEnumSharedSingletonInvokeArgs$Helper;

.method static constructor <clinit>()V
    .registers 1
    new-instance v0, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;
    invoke-direct {v0}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;-><init>()V
    sput-object v0, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->INSTANCE:Lenums/TestEnumSharedSingletonInvokeArgs$Helper;
    return-void
.end method

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static format(I)Ljava/lang/String;
    .registers 1
    const-string p0, "formatted"
    return-object p0
.end method

.method public static format(I[Ljava/lang/Object;)Ljava/lang/String;
    .registers 2
    const-string p0, "formatted"
    return-object p0
.end method

.method public final getName()Ljava/lang/String;
    .registers 1
    const-string p0, "helper"
    return-object p0
.end method
