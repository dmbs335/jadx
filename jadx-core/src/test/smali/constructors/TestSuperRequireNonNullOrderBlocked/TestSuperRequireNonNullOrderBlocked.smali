.class public Lconstructors/TestSuperRequireNonNullOrderBlocked;
.super Lconstructors/TestSuperRequireNonNullOrderBlockedParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 3
    const-string v0, "value"
    invoke-static {p1, v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-static {}, Lconstructors/TestSuperRequireNonNullOrderBlocked;->sideEffect()I
    move-result v1
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v2
    invoke-direct {p0, v1, v2}, Lconstructors/TestSuperRequireNonNullOrderBlockedParent;-><init>(II)V
    return-void
.end method

.method private static sideEffect()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method
