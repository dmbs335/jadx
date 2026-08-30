.class public Lconstructors/TestConstructorOrderedIdentityChecksInline;
.super Lconstructors/TestConstructorOrderedIdentityChecksInlineParent;

.method public constructor <init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .locals 2
    invoke-static {p1}, Lconstructors/TestConstructorOrderedIdentityChecksInline;->factoryA(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    invoke-static {}, Lconstructors/TestConstructorOrderedIdentityChecksInline;->factoryB()Ljava/lang/Object;
    move-result-object v1
    invoke-static {p2}, Lcom/google/android/gms/common/internal/Preconditions;->checkNotNull(Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {p3}, Lcom/google/android/gms/common/internal/Preconditions;->checkNotNull(Ljava/lang/Object;)Ljava/lang/Object;
    invoke-direct {p0, v0, v1, p2, p3}, Lconstructors/TestConstructorOrderedIdentityChecksInlineParent;-><init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    return-void
.end method

.method private static factoryA(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static factoryB()Ljava/lang/Object;
    .locals 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method
