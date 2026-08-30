.class public Lconstructors/TestConstructorOrderedInvokeBlocked;
.super Lconstructors/TestConstructorOrderedInvokeBlockedParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 1

    invoke-static {p1}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I
    move-result v0
    invoke-static {}, Lconstructors/TestConstructorOrderedInvokeBlocked;->sideEffect()V
    invoke-direct {p0, v0}, Lconstructors/TestConstructorOrderedInvokeBlockedParent;-><init>(I)V
    return-void
.end method

.method private static sideEffect()V
    .locals 0

    return-void
.end method
