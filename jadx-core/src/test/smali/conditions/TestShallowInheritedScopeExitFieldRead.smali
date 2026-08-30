.class public final Lconditions/TestShallowInheritedScopeExitFieldRead;
.super Ljava/lang/Object;

.field private final mask:I

.method public constructor <init>(I)V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput p1, p0, Lconditions/TestShallowInheritedScopeExitFieldRead;->mask:I
    return-void
.end method

.method public test(Ljava/lang/Integer;)Z
    .registers 5

    if-eqz p1, :outside

    iget v0, p0, Lconditions/TestShallowInheritedScopeExitFieldRead;->mask:I
    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I
    move-result v1
    const/4 v2, 0x1
    shl-int/2addr v2, v1
    and-int/2addr v2, v0
    if-eqz v2, :outside

    const/4 v0, 0x1
    return v0

    :outside
    const/4 v0, 0x0
    return v0
.end method
