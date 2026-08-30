.class public Lconstructors/TestConstructorOrderedIdentityChecksOrderBlocked;
.super Lconstructors/TestConstructorOrderedIdentityChecksOrderBlockedParent;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 1
    invoke-static {}, Lconstructors/TestConstructorOrderedIdentityChecksOrderBlocked;->factory()Ljava/lang/Object;
    move-result-object v0
    invoke-static {p1}, Lcom/google/android/gms/common/internal/Preconditions;->checkNotNull(Ljava/lang/Object;)Ljava/lang/Object;
    invoke-direct {p0, p1, v0}, Lconstructors/TestConstructorOrderedIdentityChecksOrderBlockedParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method

.method private static factory()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method
