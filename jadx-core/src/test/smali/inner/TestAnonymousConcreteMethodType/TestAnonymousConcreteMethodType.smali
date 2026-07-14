.class public Linner/TestAnonymousConcreteMethodType;
.super Ljava/lang/Object;

.annotation runtime Lkotlin/Metadata;
.end annotation

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Linner/TestAnonymousConcreteMethodType$1;
    invoke-direct {v0, p0}, Linner/TestAnonymousConcreteMethodType$1;-><init>(Linner/TestAnonymousConcreteMethodType;)V
    invoke-static {v0}, Linner/TestAnonymousConcreteMethodType;->capture(Linner/TestAnonymousConcreteMethodType$1;)V
    return-void
.end method

.method private static capture(Linner/TestAnonymousConcreteMethodType$1;)V
    .registers 1
    invoke-static {p0}, Linner/TestAnonymousConcreteMethodType;->use(Ljava/lang/Thread;)V
    return-void
.end method

.method private static use(Ljava/lang/Thread;)V
    .registers 1
    return-void
.end method
