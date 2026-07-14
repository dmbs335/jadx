.class public Linner/TestAnonymousConcreteFieldType;
.super Ljava/lang/Object;

.field private final callback:Linner/TestAnonymousConcreteFieldType$1;

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    new-instance v0, Linner/TestAnonymousConcreteFieldType$1;
    invoke-direct {v0, p0}, Linner/TestAnonymousConcreteFieldType$1;-><init>(Linner/TestAnonymousConcreteFieldType;)V
    iput-object v0, p0, Linner/TestAnonymousConcreteFieldType;->callback:Linner/TestAnonymousConcreteFieldType$1;
    invoke-static {v0}, Linner/TestAnonymousConcreteFieldType;->use(Ljava/lang/Thread;)V
    return-void
.end method

.method private static use(Ljava/lang/Thread;)V
    .registers 1
    return-void
.end method

.method public shutdown()V
    .registers 2
    iget-object v0, p0, Linner/TestAnonymousConcreteFieldType;->callback:Linner/TestAnonymousConcreteFieldType$1;
    invoke-static {v0}, Linner/TestAnonymousConcreteFieldType;->use(Ljava/lang/Thread;)V
    return-void
.end method
