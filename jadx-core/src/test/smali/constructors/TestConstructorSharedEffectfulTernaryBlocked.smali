.class public Lconstructors/TestConstructorSharedEffectfulTernaryBlocked;
.super Ljava/lang/Object;

.method public constructor <init>(ZI)V
    .locals 1

    and-int/lit8 v0, p2, 0x1
    if-eqz v0, :done
    invoke-static {}, Lconstructors/TestConstructorSharedEffectfulTernaryBlocked;->next()Z
    move-result p1

    :done
    invoke-direct {p0, p1, p1}, Lconstructors/TestConstructorSharedEffectfulTernaryBlocked;-><init>(ZZ)V
    return-void
.end method

.method public constructor <init>(ZZ)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private static next()Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method
