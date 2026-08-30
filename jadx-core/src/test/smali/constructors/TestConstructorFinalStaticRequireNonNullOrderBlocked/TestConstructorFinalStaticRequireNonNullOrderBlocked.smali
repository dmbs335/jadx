.class public Lconstructors/TestConstructorFinalStaticRequireNonNullOrderBlocked;
.super Lconstructors/TestConstructorFinalStaticRequireNonNullOrderBlockedParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 2
    sget-object v0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;
    const-string v1, "value"
    invoke-static {p1, v1}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-direct {p0, p1, v0}, Lconstructors/TestConstructorFinalStaticRequireNonNullOrderBlockedParent;-><init>(Ljava/lang/String;Ljava/util/List;)V
    return-void
.end method
