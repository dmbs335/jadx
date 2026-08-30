.class public Lconstructors/TestConstructorInvokeFinalStaticOrderBlocked;
.super Lconstructors/TestConstructorInvokeFinalStaticOrderBlockedParent;

.method public constructor <init>()V
    .locals 2
    invoke-static {}, Lconstructors/TestConstructorInvokeFinalStaticOrderBlocked;->factory()Ljava/lang/Object;
    move-result-object v0
    sget-object v1, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;
    invoke-direct {p0, v1, v1, v0}, Lconstructors/TestConstructorInvokeFinalStaticOrderBlockedParent;-><init>(Ljava/util/List;Ljava/util/List;Ljava/lang/Object;)V
    return-void
.end method

.method private static factory()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method
